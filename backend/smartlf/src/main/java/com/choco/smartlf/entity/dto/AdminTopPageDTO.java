package com.choco.smartlf.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "管理员-置顶申请分页查询参数")
public class AdminTopPageDTO {
    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页记录数", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "申请状态 (0:待审核, 1:已通过, 2:已拒绝)，不传则查全部")
    private Integer status;
}