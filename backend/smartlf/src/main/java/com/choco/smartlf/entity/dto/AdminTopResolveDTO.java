package com.choco.smartlf.entity.dto;

import com.choco.smartlf.enums.TopApplyStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "管理员-处理置顶申请参数")
public class AdminTopResolveDTO {
    @NotNull(message = "申请记录ID不能为空")
    @Schema(description = "申请记录ID")
    private Long applyId;

    @NotNull(message = "处理动作不能为空")
    @Schema(description = "处理动作 (0:同意置顶, 1:拒绝)")
    private Integer action;

    @Schema(description = "管理员处理备注(可选)")
    private String remark;
}