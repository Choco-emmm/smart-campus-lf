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
 * 物品核心主表
 * </p>
 *
 * @author 
 * @since 2026-04-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("item_info")
@ApiModel(value="ItemInfo对象", description="物品核心主表")
public class ItemInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "物品主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "发布者用户ID")
    private Long userId;

    @ApiModelProperty(value = "0:丢失，1:拾取")
    private Integer type;

    @ApiModelProperty(value = "物品标题")
    private String itemName;

    @ApiModelProperty(value = "丢失/拾取发生时间")
    private LocalDateTime eventTime;

    @ApiModelProperty(value = "发生地点")
    private String location;

    @ApiModelProperty(value = "公开可见的简述")
    private String publicDesc;

    @ApiModelProperty(value = "0:寻找中，1:锁定中，2:已结案")
    private Integer status;

    @ApiModelProperty(value = "是否置顶（0:否，1:是）")
    private Integer isTop;

    @ApiModelProperty(value = "置顶结束时间")
    private LocalDateTime topEndTime;

    @ApiModelProperty(value = "1:高价值/敏感（强制二审）")
    private Integer isSensitive;

    @ApiModelProperty(value = "被举报次数")
    private Integer reportCount;

    private LocalDateTime createTime;


}
