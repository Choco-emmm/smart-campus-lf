package com.choco.smartlf.enums;

import lombok.Getter;

/**
 * 全局 WebSocket 应用层消息类型枚举
 */
@Getter
public enum WebSocketMsgTypeEnum {
    ACTIVE_WINDOW("active_window", "前端焦点状态上报"),
    CHAT("chat", "私聊消息"),
    NOTICE("notice", "帖子留言提醒通知"),
    CLAIM("claim", "认领申请进度变更通知"),
    AI_POLISH_FINISH("ai_polish_finish", "大模型异步润色完成通知");

    private final String type;
    private final String desc;

    WebSocketMsgTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}