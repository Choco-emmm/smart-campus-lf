package com.choco.smartlf.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public enum ResultCodeEnum {
    // ---- 基础系统码 ----
    SUCCESS(1, "success"),
    FAIL(0, "操作失败"),
    UNAUTHORIZED(401, "您还未登录或登录已过期"),
    FORBIDDEN(403, "权限不足，禁止访问"),
    BAD_REQUEST(400, "请求参数错误"),

    // ---- 01. 用户模块 (1000段) ----
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户名、邮箱或手机号已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    PASSWORD_SAME_ERROR(1004, "新密码不能与旧密码相同"),
    PHONE_EXIST(1005, "该手机号已被绑定"),
    EMAIL_EXIST(1006, "该邮箱已被绑定"),
    USER_BANNED(1007, "用户已封禁！请联系管理员"),
    ADMIN_KEY_ERROR(1008, "管理员密钥错误"),

    // ---- 02. 文件与通用 (2000段) ----
    INVALID_FILE(2001, "文件格式不正确或为空"),
    FILE_UPLOAD_ERROR(2002, "文件上传失败");

    private final Integer code;
    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}