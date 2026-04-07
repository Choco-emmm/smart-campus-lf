package com.choco.smartlf.entity;

import com.choco.smartlf.enums.ResultCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "全局统一响应结果封装")
public class Result<T> {

    @Schema(description = "业务状态码（1:成功, 其他:失败）", example = "1")
    private Integer code;

    @Schema(description = "提示信息", example = "success")
    private String msg;

    @Schema(description = "响应的具体数据负载")
    private T data;

    // --- 成功 ---
    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCodeEnum.SUCCESS.getCode());
        result.setMsg(ResultCodeEnum.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    // --- 失败 ---
    // 1. 直接返回底层 code 和 msg (被全局异常拦截器使用)
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    // 2. 传入单个字符串 (默认状态码 0)
    public static <T> Result<T> error(String msg) {
        return error(ResultCodeEnum.FAIL.getCode(), msg);
    }

    // 3. 传入枚举
    public static <T> Result<T> error(ResultCodeEnum resultCodeEnum) {
        return error(resultCodeEnum.getCode(), resultCodeEnum.getMessage());
    }
}