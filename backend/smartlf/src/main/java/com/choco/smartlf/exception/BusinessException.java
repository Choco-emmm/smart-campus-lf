package com.choco.smartlf.exception;

import com.choco.smartlf.enums.ErrorMsgEnum;
import com.choco.smartlf.enums.ResultCodeEnum;
import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCodeEnum.FAIL.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

    public BusinessException(ErrorMsgEnum errorMsgEnum) {
        super(errorMsgEnum.getMessage());
        this.code = ResultCodeEnum.FAIL.getCode();
    }

    public BusinessException(ResultCodeEnum resultCodeEnum, ErrorMsgEnum errorMsgEnum) {
        super(errorMsgEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

    public BusinessException(ResultCodeEnum resultCodeEnum, String message) {
        super(message);
        this.code = resultCodeEnum.getCode();
    }
}