package com.choco.smartlf.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMsgEnum {
    // 用户相关错误
    USER_NOT_FOUND("用户不存在"),
    USER_ALREADY_EXISTS("用户已存在"),
    USER_NOT_LOGGED_IN("用户未登录"),
    USER_NOT_AUTHORIZED("用户未授权"),
    
    // 认证相关错误
    INVALID_PASSWORD("密码错误"),
    INVALID_USERNAME("用户名错误"),
    INVALID_USERNAME_OR_PASSWORD("用户名或密码错误"),
    INVALID_USERNAME_OR_EMAIL_OR_PHONE("用户名、邮箱或手机号错误"),
    ADMIN_SECRET_KEY_ERROR("管理员密钥错误"),
    
    // 格式验证错误
    INVALID_PHONE("手机号格式不正确"),
    INVALID_EMAIL("邮箱格式不正确"),
    INVALID_NICKNAME("昵称格式不正确"),
    INVALID_FILE("文件格式不正确"),
    
    // 重复性错误
    PHONE_EXIST("手机号已存在"),
    EMAIL_EXIST("邮箱已存在"),
    USERNAME_EMAIL_OR_PHONE_EXIST("用户名、邮箱或手机号已存在"),
    
    // 密码相关错误
    PASSWORD_ERROR("旧密码错误"),
    PASSWORD_SAME_ERROR("新密码不能与旧密码相同"),
    
    // 文件相关错误
    FILE_UPLOAD_ERROR("文件上传错误"),
    
    // 权限相关错误
    PERMISSION_DENIED_STUDENT("权限不足：仅限普通学生访问"),
    PERMISSION_DENIED_ADMIN("权限越界：非管理员禁止访问"),
    
    // Token相关错误
    TOKEN_NOT_LOGIN("您还未登录，请先登录"),
    TOKEN_INVALID("登录状态异常，请重新登录"),
    TOKEN_EXPIRED("登录已过期，请重新登录"),
    
    // 业务状态错误
    USER_BANNED("用户已封禁！请联系管理员");

    private final String message;

    @Override
    public String toString() {
        return message;
    }
}