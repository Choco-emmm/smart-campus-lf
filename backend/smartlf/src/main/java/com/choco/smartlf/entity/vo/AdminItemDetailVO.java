package com.choco.smartlf.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true) // 保证 equals 方法包含父类属性
@Schema(description = "管理员专属帖子详情视图 ")
public class AdminItemDetailVO extends ItemDetailVO {

    // 🌟 在基础详情之上，补充管理员需要看到的【核验明细】字段

    @Schema(description = "核验问题/图片描述")
    private String checkQuestion;

    @Schema(description = "核验答案")
    private String checkAnswer;

    @Schema(description = "发帖人预留的联系方式")
    private String contactInfo;

}