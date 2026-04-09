package com.choco.smartlf.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理员-平台统计数据面板")
public class AdminStatsVO {
    
    @Schema(description = "总发帖量")
    private Long totalItems;

    @Schema(description = "已结案(找回)数量")
    private Long solvedItems;

    @Schema(description = "整体找回率(百分比字符串)")
    private String retrieveRate;

    @Schema(description = "今日活跃用户数")
    private Long dailyActiveUsers;
}