package com.choco.smartlf.enums;

import lombok.Getter;

/**
 * 消息/留言阅读状态枚举
 */
@Getter
public enum ReadStatusEnum {

    UNREAD(0, "未读"),
    READ(1, "已读");

    private final Integer code;
    private final String desc;

    ReadStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}