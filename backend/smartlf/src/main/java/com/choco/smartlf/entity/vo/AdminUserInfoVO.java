package com.choco.smartlf.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理员视角-用户详细档案")
public class AdminUserInfoVO {

    // ================= 基础信息 (与 UserInfoVO 一致) =================
    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "注册时间")
    private LocalDateTime createTime;

    // ================= 管理员专属敏感/统计信息 =================
    @Schema(description = "账号状态 (0-正常, 1-封禁)")
    private Integer status;

    @Schema(description = "最后活跃时间")
    private LocalDateTime lastActiveTime;

    @Schema(description = "违规发帖次数 (被下架的帖子数量)")
    private Integer violationCount;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;
}