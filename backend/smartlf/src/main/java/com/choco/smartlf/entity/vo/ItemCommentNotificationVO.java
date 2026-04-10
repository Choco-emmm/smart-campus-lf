package com.choco.smartlf.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "留言提醒视图")
public class ItemCommentNotificationVO {

    @Schema(description = "帖子ID")
    private Long itemId;

    @Schema(description = "帖子标题")
    private String itemTitle;

    @Schema(description = "该帖子的未读留言总数")
    private Integer unreadCount;

    @Schema(description = "最新一条留言的内容摘要")
    private String lastCommentContent;

    // 前端根据这个组合出：你发表的“{{itemTitle}}”失物信息有{{unreadCount}}条未读留言
}