package com.choco.smartlf.entity.dto;

import com.choco.smartlf.utils.Constant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "修改密码请求参数")
public class UpdatePasswordDTO {
    @NotBlank(message = "原密码不能为空")
    @Schema(description = "原密码", required = true)
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = Constant.PASSWORD_REGEX, message = "新密码格式不正确（6-20位，至少包含一个字母和一个数字）")
    @Schema(description = "新密码", required = true)
    private String newPassword;
}