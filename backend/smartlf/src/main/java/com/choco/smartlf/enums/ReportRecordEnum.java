package com.choco.smartlf.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportRecordEnum {
    WAITING (0, "待处理"),
    VERIFYING (1, "核实"),
    REJECTED (2, "驳回");


    private final Integer code;
    private final String description;

    public static ReportRecordEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReportRecordEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的举报单类型: " + code);
    }
}
