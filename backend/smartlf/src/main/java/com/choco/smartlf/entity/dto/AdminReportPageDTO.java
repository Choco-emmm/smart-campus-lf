package com.choco.smartlf.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "管理员-举报分页查询参数")
public class AdminReportPageDTO {
    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页记录数", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "举报状态 (0:待处理, 1:已核实, 2:已驳回)，不传则查全部")
    private Integer status;

    @Schema(description = "举报理由")
    private String reason;
}