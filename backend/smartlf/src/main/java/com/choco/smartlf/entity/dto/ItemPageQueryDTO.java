package com.choco.smartlf.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "物品分页查询参数")
public class ItemPageQueryDTO {
    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页记录数", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "物品类型 (0:丢失, 1:拾取)")
    private Integer type;

    @Schema(description = "搜索关键词 (匹配标题或内容)")
    private String keyword;
}