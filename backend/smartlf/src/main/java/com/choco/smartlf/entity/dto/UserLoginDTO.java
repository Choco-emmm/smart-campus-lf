package com.choco.smartlf.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Schema(description = "用户登录请求参数")
public class UserLoginDTO {

    @NotBlank(message = "账号不能为空")
    @Schema(description = "账号 (支持搜索ID/手机号/邮箱登录)", required = true, example = "Choco2026")
    private String account;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", required = true)
    private String password;
}