package com.choco.smartlf.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.tools.Diagnostic;

@Getter
@AllArgsConstructor
public enum TopApplyStatusEnum {
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝"),
    PENDING(0, "待审核");
    private final Integer code;
    private final String description;
}