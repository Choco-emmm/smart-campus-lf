package com.choco.smartlf.entity.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ItemSecure {
    @Schema(description="关联主表ID")
    @TableId(type = IdType.INPUT)
    private Long itemId;
    @Schema(description="核验暗号")
    private String verifyAnswer;
    @Schema(description="拾取者的真实联系方式")
    private String privateContact;
}
