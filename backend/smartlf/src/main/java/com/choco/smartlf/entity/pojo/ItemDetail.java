package com.choco.smartlf.entity.pojo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ItemDetail {
    @Schema(description="关联主表ID")
    private Long itemId;
    @Schema(description="半公开细节（注册满24小时可见）")
    private String semiPublicDesc;
    @Schema(description="图片URL数组（JSON字符串形式）")
    private String imagesUrl;
    @Schema(description="AI辅助生成的描述备份")
    private String aiGeneratedDesc;
}
