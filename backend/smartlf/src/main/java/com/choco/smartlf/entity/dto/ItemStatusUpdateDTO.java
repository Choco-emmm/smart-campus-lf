package com.choco.smartlf.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "修改物品状态请求参数")
public class ItemStatusUpdateDTO {
    @NotNull(message = "物品ID不能为空")
    private Long id;

    @NotNull(message = "目标状态不能为空")
    @Schema(description = "0:寻找中，1:锁定中，2:已结案")
    private Integer status;
}