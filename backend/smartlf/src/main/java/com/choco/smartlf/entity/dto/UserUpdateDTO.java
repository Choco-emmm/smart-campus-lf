package com.choco.smartlf.entity.dto;

import com.choco.smartlf.utils.Constant;
import com.choco.smartlf.utils.ErrorMsgConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "修改个人信息请求参数")
public class UserUpdateDTO {
    @Pattern(regexp = Constant.NICKNAME_REGEX, message = ErrorMsgConstant.INVALID_NICKNAME)
    @Schema(description = "自定义昵称")
    private String nickname;

    @Pattern(regexp = Constant.PHONE_REGEX, message = ErrorMsgConstant.INVALID_PHONE)
    @Schema(description = "手机号")
    private String phone;

    @Pattern(regexp = Constant.EMAIL_REGEX, message = ErrorMsgConstant.INVALID_EMAIL)
    @Schema(description = "邮箱")
    private String email;
}