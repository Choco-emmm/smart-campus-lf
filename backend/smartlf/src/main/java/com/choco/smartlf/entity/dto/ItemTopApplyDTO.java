package com.choco.smartlf.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "物品置顶申请参数")
public class ItemTopApplyDTO {

    @NotNull(message = "物品ID不能为空")
    @Schema(description = "申请置顶的物品ID", required = true)
    private Long itemId;

    @NotBlank(message = "申请理由不能为空")
    @Schema(description = "申请理由（如：极其贵重的物品、包含机密数据等）", required = true)
    private String applyReason;
}