package com.choco.smartlf.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

@Data
@Schema(description = "物品列表视图对象 (轻量级)")
public class ItemListVO {
    private Long id;
    private Integer type;
    private String itemName;
    private Date eventTime;
    private String location;
    private String publicDesc;
    private Integer status;
    private Integer isTop;
    
    @Schema(description = "封面图（取详情表里的第一张图）")
    private String coverImage;
    
    private Date createTime;
}