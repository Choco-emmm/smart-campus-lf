package com.choco.smartlf.enums;

import lombok.Getter;

/**
 * 全局 WebSocket 应用层消息类型枚举（纯净通道版）
 */
@Getter
public enum WebSocketMsgTypeEnum {
    // 1. 系统底座通道（心跳与状态）
    ACTIVE_WINDOW("active_window", "前端焦点状态上报"),

    // 2. 聊天互动通道（重度交互：需要存数据库、展示在长连接聊天列表区）
    CHAT("chat", "私聊消息通道"),

    // 3. 全局系统弹窗通道（轻度交互：只需在右下角弹出即可）
    // 涵盖：留言提醒、认领进度变更、AI处理完成、审核结果通知等所有场景
    NOTICE("notice", "全局系统弹窗通知通道");

    private final String type;
    private final String desc;

    WebSocketMsgTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}