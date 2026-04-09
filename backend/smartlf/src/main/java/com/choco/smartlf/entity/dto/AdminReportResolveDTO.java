package com.choco.smartlf.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "管理员-处理举报参数")
public class AdminReportResolveDTO {
    @NotNull(message = "举报记录ID不能为空")
    @Schema(description = "举报记录ID")
    private Long reportId;

    @NotNull(message = "处理动作不能为空")
    @Schema(description = "处理动作 (0:核实违规并下架, 1:驳回举报)")
    private Integer action;

    @Schema(description = "管理员处理备注")
    private String remark;
}