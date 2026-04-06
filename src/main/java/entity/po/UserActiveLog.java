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
 * 用户活跃流水表
 * </p>
 *
 * @author 
 * @since 2026-04-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_active_log")
@ApiModel(value="UserActiveLog对象", description="用户活跃流水表")
public class UserActiveLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "流水主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "活跃用户ID")
    private Long userId;

    @ApiModelProperty(value = "具体活跃时间点（B+树索引）")
    private LocalDateTime activeTime;


}
