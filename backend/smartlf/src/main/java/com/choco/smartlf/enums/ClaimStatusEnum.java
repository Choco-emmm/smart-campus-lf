package com.choco.smartlf.enums;

import lombok.Getter;

/**
 * 认领申请单状态枚举
 */
@Getter
public enum ClaimStatusEnum {
    PENDING(0, "待审核(首次提交)"),
    APPROVED(1, "已同意"),
    REJECTED(2, "已拒绝"),
    REQUIRE_SUPPLEMENT(3, "需补充证据(发布者追问)"),
    SUPPLEMENT_SUBMITTED(4, "补充已提交(等待最终审核)");

    private final Integer code;
    private final String desc;

    ClaimStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取描述（可用于简单的转换）
     */
    public static String getDescByCode(Integer code) {
        for (ClaimStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status.getDesc();
            }
        }
        return "未知状态";
    }
}