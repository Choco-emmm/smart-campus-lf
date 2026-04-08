package com.choco.smartlf.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum {
    USER(0, "普通用户"),
    ADMIN(1, "管理员");

    private final Integer code;
    private final String description;

    public static RoleEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("无效的角色类型: " + code);
    }
}
