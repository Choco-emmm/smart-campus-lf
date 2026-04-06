package com.choco.smartlf.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "用户登录成功返回的视图数据")
public class UserLoginVO {

    @Schema(description = "身份令牌 (后续所有请求需放在Header的token字段中)")
    private String token;

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "唯一搜索ID")
    private String username;

    @Schema(description = "展示昵称")
    private String nickname;

    @Schema(description = "角色 (0-学生, 1-管理员)")
    private Integer role;
}