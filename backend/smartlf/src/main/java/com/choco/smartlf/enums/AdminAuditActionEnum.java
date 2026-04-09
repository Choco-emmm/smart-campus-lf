package com.choco.smartlf.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AdminAuditActionEnum {
    PASS(1, "审核通过（核实/同意）"),
    REJECT(2, "审核驳回（拒绝）");

    private final Integer code;
    private final String description;
}