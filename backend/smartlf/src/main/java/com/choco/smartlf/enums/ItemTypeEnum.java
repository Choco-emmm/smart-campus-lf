package com.choco.smartlf.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemTypeEnum {
    LOST(0, "丢失"),
    FOUND(1, "拾取");

    private final Integer code;
    private final String description;

    public static ItemTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ItemTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的物品类型: " + code);
    }
}