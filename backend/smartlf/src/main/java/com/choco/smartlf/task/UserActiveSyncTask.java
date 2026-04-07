package com.choco.smartlf.task;

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

import java.util.ArrayList;
import java.util.Date;
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
        // 1. 简单粗暴：直接把 Redis 里的活跃数据全部捞出来
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(ACTIVE_TIME_KEY);
        
        // 如果这 10 分钟内没人活跃，直接结束
        if (entries.isEmpty()) {
            return;
        }

        // 2. 捞完数据后，立刻把 Redis 里的这个 Key 删掉，迎接下一波新数据
        stringRedisTemplate.delete(ACTIVE_TIME_KEY);

        List<User> userUpdates = new ArrayList<>();
        List<UserActiveLog> logInserts = new ArrayList<>();

        // 3. 遍历刚才捞出来的数据，准备存库
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Long userId = Long.valueOf(entry.getKey().toString());
            Date activeDate = new Date(Long.parseLong(entry.getValue().toString()));

            // 组装 User 实体（用于更新最后活跃时间）
            User user = new User();
            user.setId(userId);
            user.setLastActiveTime(activeDate);
            userUpdates.add(user);

            // 组装 流水表 实体（用于管理员按时间段查询）
            UserActiveLog logItem = new UserActiveLog();
            logItem.setUserId(userId);
            logItem.setActiveTime(activeDate);
            logInserts.add(logItem);
        }

        // 4. 批量执行数据库操作
        userService.updateBatchById(userUpdates);
        userActiveLogService.saveBatch(logInserts);
        
        log.info("定时任务执行完毕：成功同步了 {} 个用户的活跃记录", userUpdates.size());
    }
}