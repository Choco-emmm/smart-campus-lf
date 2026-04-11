package com.choco.smartlf.task;

import com.choco.smartlf.service.ItemInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AIReportTask {

    private final ItemInfoService itemInfoService;

    /**
     * 定期调用 AI 生成总结报告
     * cron = "0 0 2 * * ?" 表示：每天凌晨 2 点 0 分 0 秒自动执行一次
     */
    @Scheduled(cron = "0 0 2 * * 0")
    public void autoGenerateWeeklyReport() {
        log.info("【AI巡检】开始执行定时总结任务...");
        try {
            itemInfoService.generateAdminSummary();
        } catch (Exception e) {
            log.error("【AI巡检】大模型生成报告失败", e);
        }
    }
}