package com.choco.smartlf.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemStatusEnum {
    SEARCHING(0, "寻找中"),
    LOCKED(1, "锁定中"),
    CLOSED(2, "已结案"),
    BANNED(3, "违规下架");

    private final Integer code;
    private final String description;

    public static ItemStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ItemStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的物品状态: " + code);
    }
}