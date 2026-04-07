package com.choco.smartlf.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "物品举报请求参数")
public class ItemReportDTO {

    @NotNull(message = "被举报物品ID不能为空")
    @Schema(description = "被举报的物品ID", required = true)
    private Long itemId;

    @NotBlank(message = "举报理由不能为空")
    @Schema(description = "举报理由（如：虚假信息、诈骗、恶意内容等）", required = true)
    private String reason;
}