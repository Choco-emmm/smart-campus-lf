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

    @Schema(description = "搜索关键词 (匹配公开字或物品名称)")
    private String keyword;

    @Schema(description = "物品状态 0:寻找中，1:锁定中，2:已结案")
    private Integer status;

    @Schema(description = "物品地点")
    private String location;
}