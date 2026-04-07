package com.choco.smartlf.entity.dto;

import com.choco.smartlf.utils.Constant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "用户注册请求参数")
public class UserRegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = Constant.USERNAME_REGEX, message = "用户名格式不正确")
    @Schema(description = "用户名，4-16位，允许字母、数字、下划线、减号", required = true, example = "Choco2026")
    private String username;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = Constant.PHONE_REGEX, message = "手机号格式不正确")
    @Schema(description = "手机号", required = true)
    private String phone;

    @NotBlank(message = "邮箱不能为空")
    @Pattern(regexp = Constant.EMAIL_REGEX, message = "邮箱格式不正确")
    @Schema(description = "邮箱", required = true)
    private String email;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = Constant.PASSWORD_REGEX, message = "密码格式不正确")
    @Schema(description = "密码，6-20位，至少包含一个字母和一个数字", required = true)
    private String password;

    @NotNull(message = "角色不能为空")
    @Min(value = 0, message = "角色格式不正确")
    @Max(value = 1, message = "角色格式不正确")
    @Schema(description = "角色（0-学生，1-管理员）", required = true, example = "0")
    private Integer role;

    @Schema(description = "管理员授权密钥（仅注册管理员时需填写）", required = false)
    private String secretKey;
}