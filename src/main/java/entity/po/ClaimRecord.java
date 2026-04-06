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
 * 认领申请记录表
 * </p>
 *
 * @author 
 * @since 2026-04-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("claim_record")
@ApiModel(value="ClaimRecord对象", description="认领申请记录表")
public class ClaimRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "申请主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "申请的物品ID")
    private Long itemId;

    @ApiModelProperty(value = "申请人ID")
    private Long applicantId;

    @ApiModelProperty(value = "申请者填写的暗号答案")
    private String answer;

    @ApiModelProperty(value = "0:待帖主审, 1:待管理员审, 2:通过, 3:拒绝")
    private Integer status;

    @ApiModelProperty(value = "申请时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "最后修改时间")
    private LocalDateTime updateTime;


}
