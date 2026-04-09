package com.choco.smartlf.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理员-举报单详情")
public class AdminReportDetailVO {

    @Schema(description = "举报单ID")
    private Long reportId;

    @Schema(description = "举报理由")
    private String reason;

    @Schema(description = "举报人昵称")
    private String reporterNickname;

    @Schema(description = "帖子标题 (原帖题目)")
    private String itemTitle;

    @Schema(description = "原帖ID (供前端做跳转超链接使用)")
    private Long itemId;
}