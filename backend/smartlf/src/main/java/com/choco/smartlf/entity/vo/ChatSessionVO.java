package com.choco.smartlf.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "私信会话列表视图")
public class ChatSessionVO {
    @Schema(description = "聊天对象的ID")
    private Long targetUserId;

    @Schema(description = "聊天对象的头像")
    private String targetAvatar;

    @Schema(description = "聊天对象的昵称")
    private String targetNickname;

    @Schema(description = "最后一条消息的内容")
    private String lastMessage;

    @Schema(description = "最后一条消息的发送时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "当前会话的未读消息数")
    private Integer unreadCount;
}