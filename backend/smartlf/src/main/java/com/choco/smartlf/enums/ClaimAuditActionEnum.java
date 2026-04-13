package com.choco.smartlf.enums;

import lombok.Getter;

@Getter
public enum ClaimAuditActionEnum {
    //1:同意, 2:拒绝, 3:要求补充
    AGREE(1, "同意"),
    REJECT(2, "拒绝"),
    REQUIRE_SUPPLEMENT(3, "要求补充");
    private final Integer code;
    private final String message;
    ClaimAuditActionEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
