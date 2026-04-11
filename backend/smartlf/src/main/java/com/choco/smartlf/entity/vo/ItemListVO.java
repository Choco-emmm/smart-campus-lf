package com.choco.smartlf.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Schema(description = "物品列表视图对象 (轻量级)")
public class ItemListVO {
    private Long id;
    private Integer type;
    @Schema(description = "物品标签/核心名 (前端可作为一个小小的 Tag 展示)", example = "雨伞")
    private String itemName;
    private LocalDateTime eventTime;
    private String location;
    @Schema(description = "帖子大标题 (前端用加粗大字体展示在封面图下方)", example = "在二饭捡到一把黑色雨伞")
    private String publicDesc;
    private Integer role;
    private Integer status;
    private Integer isTop;
    
    @Schema(description = "封面图（取详情表里的第一张图）")
    private String coverImage;
    
    private LocalDateTime createTime;
}