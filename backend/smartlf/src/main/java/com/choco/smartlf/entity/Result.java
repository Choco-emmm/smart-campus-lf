package com.choco.smartlf.entity;

import com.choco.smartlf.enums.ResultCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后端统一返回的结果封装类
 */
@Data
@Schema(description = "全局统一响应结果封装")
public class Result<T> {

    @Schema(description = "业务状态码（1:成功, 其他:失败）", example = "1")
    private Integer code;

    @Schema(description = "提示信息", example = "success")
    private String msg;

    @Schema(description = "响应的具体数据负载")
    private T data;

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = ResultCodeEnum.SUCCESS.getCode();
        result.msg = ResultCodeEnum.SUCCESS.getMessage();
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.data = data;
        result.code = ResultCodeEnum.SUCCESS.getCode();
        result.msg = ResultCodeEnum.SUCCESS.getMessage();
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.msg = msg;
        result.code = ResultCodeEnum.FAIL.getCode();
        return result;
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.code = code;
        result.msg = msg;
        return result;
    }

    public static <T> Result<T> error(ResultCodeEnum resultCodeEnum) {
        Result<T> result = new Result<>();
        result.code = resultCodeEnum.getCode();
        result.msg = resultCodeEnum.getMessage();
        return result;
    }

    public static <T> Result<T> error(ResultCodeEnum resultCodeEnum, String msg) {
        Result<T> result = new Result<>();
        result.code = resultCodeEnum.getCode();
        result.msg = msg;
        return result;
    }
}