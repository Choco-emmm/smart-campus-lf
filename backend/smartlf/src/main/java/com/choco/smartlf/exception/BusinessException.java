package com.choco.smartlf.exception;

import com.choco.smartlf.enums.ResultCodeEnum;
import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;

    // 1. 传入枚举（最常用，99%的情况用这个）
    public BusinessException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

    // 2. 传入枚举 + 自定义提示（偶尔想覆盖枚举默认文字时用）
    public BusinessException(ResultCodeEnum resultCodeEnum, String customMessage) {
        super(customMessage);
        this.code = resultCodeEnum.getCode();
    }

    // 3. 纯文本兜底（兼容一些零碎的报错，状态码默认为 0）
    public BusinessException(String message) {
        super(message);
        this.code = ResultCodeEnum.FAIL.getCode();
    }
}