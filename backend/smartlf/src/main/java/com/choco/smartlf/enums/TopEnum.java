package com.choco.smartlf.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TopEnum {
    NO(0, "否"),
    YES(1, "是");

    private final Integer code;
    private final String description;

    public static TopEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TopEnum top : values()) {
            if (top.getCode().equals(code)) {
                return top;
            }
        }
        throw new IllegalArgumentException("无效的置顶标识: " + code);
    }
}