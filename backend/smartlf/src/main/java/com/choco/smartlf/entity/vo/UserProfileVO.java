package com.choco.smartlf.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户公开主页视图 (脱敏)")
public class UserProfileVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像地址")
    private String avatarUrl;

    @Schema(description = "用户角色 (0:普通学生, 1:管理员)")
    private Integer role;

    @Schema(description = "账号状态 (0:正常, 1:封禁) - 供前端展示'该用户已被封禁'标签")
    private Integer status;

    @Schema(description = "加入时间 (注册时间)")
    private LocalDateTime createTime;
}