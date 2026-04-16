package com.choco.smartlf.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "物品详情视图对象")
public class ItemDetailVO {
    private Long id;
    private Long userId; // 发布人ID
    private Integer type;
    private Integer role;
    @Schema(description = "物品标签/分类名")
    private String itemName;
    private LocalDateTime eventTime;
    private String location;
    @Schema(description = "帖子大标题")
    private String publicDesc;
    private Integer status;

    @Schema(description = "发布人昵称")
    private String publisherNickname;

    @Schema(description = "发布者头像URL")
    private String avatarUrl;
    
    @Schema(description = "半公开细节")
    private String semiPublicDesc;
    
    @Schema(description = "图片列表")
    private List<String> imagesUrlList;
    
    @Schema(description = "AI智能辅助描述")
    private String aiGeneratedDesc;

    @Schema(description = "是否开启了私密核验模式 (true:需要暗号认领, false:普通公开拿取)")
    private Boolean hasSecureCheck;

    @Schema(description = "是否开启了置顶 (true:已置顶, false:普通帖子)")
    private Boolean isTop;

    private LocalDateTime createTime;

    @Schema(description = "详细内容是否不可见(true:不可见, false:可见)")
    private Boolean isContentHidden;
}