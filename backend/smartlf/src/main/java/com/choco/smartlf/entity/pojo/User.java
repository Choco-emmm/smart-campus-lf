package com.choco.smartlf.entity.pojo;


import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class User {
    @TableId(type = IdType.AUTO)
    @Schema(description="主键")
    private Long id;
    @Schema(description="用户名，全局唯一")
    private String username;
    @Schema(description="展示昵称")
    private String nickname;
    @Schema(description="登录凭证A")
    private String email;
    @Schema(description="登录凭证B")
    private String phone;
    @Schema(description="密码，非明文存储")
    private String password;
    @Schema(description="用户头像链接")
    private String avatarUrl;
    @Schema(description="0:学生, 1:管理员", implementation = com.choco.smartlf.enums.RoleEnum.class)
    private Integer role;
    @Schema(description="0:正常, 1:封禁", implementation = com.choco.smartlf.enums.UserStatusEnum.class)
    private Integer status;
    @Schema(description="最后活跃时间")
    private Date lastActiveTime;
    @Schema(description="注册时间")
    private Date createTime;
    @Schema(description="最后修改时间")
    private Date updateTime;
    @Schema(description="逻辑删除（0:未删, 1:已删）", implementation = com.choco.smartlf.enums.DeletedEnum.class)
    private Integer isDeleted;
}
