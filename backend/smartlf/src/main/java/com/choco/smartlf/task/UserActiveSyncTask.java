package com.choco.smartlf.task;

import cn.hutool.json.JSONUtil;
import com.choco.smartlf.entity.pojo.User;
import com.choco.smartlf.entity.pojo.UserActiveLog;
import com.choco.smartlf.service.UserActiveLogService;
import com.choco.smartlf.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.choco.smartlf.utils.Constant.ACTIVE_TIME_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActiveSyncTask {

    private final StringRedisTemplate stringRedisTemplate;
    private final UserService userService;
    private final UserActiveLogService userActiveLogService;

    /**
     * 每 10 分钟执行一次同步 (600000 毫秒)
     */
    @Scheduled(fixedRate = 600000)
    @Transactional(rollbackFor = Exception.class)
    public void syncActiveTime() {
        log.info("开始执行定时任务：同步用户活跃时间");

        // 1. 获取 Redis 里当前的活跃数据
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(ACTIVE_TIME_KEY);

        if (entries.isEmpty()) {
            return;
        }

        List<User> userUpdates = new ArrayList<>();
        List<UserActiveLog> logInserts = new ArrayList<>();
        List<Object> processedKeys = new ArrayList<>(); // 记录成功处理的 Hash Key (即 userId)

        // 2. 遍历数据并组装
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {

                String userIdStr = entry.getKey().toString();
                String jsonStr = entry.getValue().toString();

                UserActiveLog userActiveLog = JSONUtil.toBean(jsonStr, UserActiveLog.class);
                Long userId = Long.valueOf(userIdStr);
                LocalDateTime activeDate = userActiveLog.getActiveTime();

                // 容错防空：防止 Hutool 序列化 LocalDateTime 失败导致空指针
                if (activeDate == null) {
                    log.warn("用户 {} 的活跃时间反序列化为空，已跳过。JSON: {}", userId, jsonStr);
                    continue;
                }

                // 组装 User 实体
                User user = new User();
                user.setId(userId);
                user.setLastActiveTime(activeDate);
                userUpdates.add(user);

                // 组装 流水表 实体
                UserActiveLog logItem = new UserActiveLog();
                logItem.setUserId(userId);
                logItem.setRole(userActiveLog.getRole());
                logItem.setActiveTime(activeDate);
                logInserts.add(logItem);

                // 标记该记录已被成功组装
                processedKeys.add(entry.getKey());
        }

        if (userUpdates.isEmpty()) {
            return;
        }

        // 3. 执行数据库批量操作
        // 如果这里数据库报错，抛出异常，触发 @Transactional 回滚
        // 并且下面的 Redis 删除代码将不会执行，数据被完美保住！
        userService.updateBatchById(userUpdates);
        userActiveLogService.saveBatch(logInserts);

        // 4. 【核心修复】数据库 100% 落库成功后，精确删除已经处理过的记录！
        // 这样即使在执行上述代码的几毫秒内有新用户活跃，新数据也不会被误删
        stringRedisTemplate.opsForHash().delete(ACTIVE_TIME_KEY, processedKeys.toArray());

        log.info("定时任务执行完毕：成功同步了 {} 个用户的活跃记录", userUpdates.size());
    }
}