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
 * 物品详情表（1对1）
 * </p>
 *
 * @author 
 * @since 2026-04-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("item_detail")
@ApiModel(value="ItemDetail对象", description="物品详情表（1对1）")
public class ItemDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "关联主表ID")
    @TableId(value = "item_id", type = IdType.AUTO)
    private Long itemId;

    @ApiModelProperty(value = "半公开细节（注册满24小时可见）")
    private String semiPublicDesc;

    @ApiModelProperty(value = "图片URL数组（JSON字符串形式）")
    private String imagesUrl;

    @ApiModelProperty(value = "AI辅助生成的描述备份")
    private String aiGeneratedDesc;


}
