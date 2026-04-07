package com.choco.smartlf.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SensitiveEnum {
    NO(0, "普通"),
    YES(1, "高价值/敏感");

    private final Integer code;
    private final String description;

    public static SensitiveEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SensitiveEnum sensitive : values()) {
            if (sensitive.getCode().equals(code)) {
                return sensitive;
            }
        }
        throw new IllegalArgumentException("无效的敏感标识: " + code);
    }
}