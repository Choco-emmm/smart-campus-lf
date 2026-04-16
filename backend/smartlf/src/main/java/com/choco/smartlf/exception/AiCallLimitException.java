package com.choco.smartlf.exception;

import com.choco.smartlf.enums.ResultCodeEnum;
import lombok.Getter;

/**
 *  ai对话时的限额异常
 */
@Getter
public class AiCallLimitException extends RuntimeException {
    private final Integer code;

    // 1. 传入枚举（最常用，99%的情况用这个）
    public AiCallLimitException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }
}
