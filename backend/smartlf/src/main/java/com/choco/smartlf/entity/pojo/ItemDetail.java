package com.choco.smartlf.entity.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ItemDetail {
    @Schema(description="关联主表ID")
    @TableId(type = IdType.INPUT)
    private Long itemId;
    @Schema(description="半公开细节（注册满24小时可见）")
    private String semiPublicDesc;
    @Schema(description="图片URL数组（JSON字符串形式）")
    private String imagesUrl;
    @Schema(description="AI辅助生成的描述备份")
    private String aiGeneratedDesc;
}
