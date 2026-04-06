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
 * 举报管理表
 * </p>
 *
 * @author 
 * @since 2026-04-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("report_record")
@ApiModel(value="ReportRecord对象", description="举报管理表")
public class ReportRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "举报主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "被举报的物品ID")
    private Long itemId;

    @ApiModelProperty(value = "举报人ID")
    private Long reporterId;

    @ApiModelProperty(value = "举报理由")
    private String reason;

    @ApiModelProperty(value = "0:待处理, 1:核实, 2:驳回")
    private Integer status;

    private LocalDateTime createTime;


}
