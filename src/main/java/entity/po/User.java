package entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户核心表
 * </p>
 *
 * @author 
 * @since 2026-04-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
@ApiModel(value="User对象", description="用户核心表")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户名，全局唯一")
    private String username;

    @ApiModelProperty(value = "展示昵称")
    private String nickname;

    @ApiModelProperty(value = "登录凭证A")
    private String email;

    @ApiModelProperty(value = "登录凭证B")
    private String phone;

    @ApiModelProperty(value = "密码，非明文存储")
    private String password;

    @ApiModelProperty(value = "用户头像链接")
    private String avatarUrl;

    @ApiModelProperty(value = "0:学生, 1:管理员")
    private Integer role;

    @ApiModelProperty(value = "0:正常, 1:封禁")
    private Integer status;

    @ApiModelProperty(value = "最后活跃时间")
    private LocalDateTime lastActiveTime;

    @ApiModelProperty(value = "注册时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "最后修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "逻辑删除（0:未删, 1:已删）")
    private Integer isDeleted;


}
