package com.choco.smartlf.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.choco.smartlf.entity.pojo.ItemInfo;
import com.choco.smartlf.enums.TopEnum;
import com.choco.smartlf.service.ItemInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemTopExpireTask {

    private final ItemInfoService itemInfoService;

    /**
     * 每 1 分钟执行一次扫描 (60000 毫秒)
     */
    @Scheduled(fixedRate = 60000)
    public void expireTopItems() {
        log.info("定时任务触发：开始扫描过期的置顶帖子...");
        // 构造更新条件：目前是置顶状态 (is_top = 1)，且过期时间早于当前时间
        LambdaUpdateWrapper<ItemInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ItemInfo::getIsTop, TopEnum.YES.getCode())
                     .lt(ItemInfo::getTopEndTime, LocalDateTime.now())
                     // 动作：把 is_top 设为 0
                     .set(ItemInfo::getIsTop, TopEnum.NO.getCode());

        // 执行批量更新
        boolean updated = itemInfoService.update(updateWrapper);
        if (updated) {
            log.info("定时任务触发：已成功将过期的置顶帖子恢复为普通帖子！");
        }
    }
}