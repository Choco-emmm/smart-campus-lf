package entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 物品安全核验表
 * </p>
 *
 * @author 
 * @since 2026-04-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("item_secure")
@ApiModel(value="ItemSecure对象", description="物品安全核验表")
public class ItemSecure implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "关联主表ID")
    @TableId(value = "item_id", type = IdType.AUTO)
    private Long itemId;

    @ApiModelProperty(value = "核验暗号")
    private String verifyAnswer;

    @ApiModelProperty(value = "拾取者的真实联系方式")
    private String privateContact;


}
