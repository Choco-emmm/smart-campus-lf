package com.choco.smartlf.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeletedEnum {
    NO(0, "未删除"),
    YES(1, "已删除");

    private final Integer code;
    private final String description;

    public static DeletedEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DeletedEnum deleted : values()) {
            if (deleted.getCode().equals(code)) {
                return deleted;
            }
        }
        throw new IllegalArgumentException("无效的删除标识: " + code);
    }
}