package com.choco.smartlf.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 0; // 默认失败状态码
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}