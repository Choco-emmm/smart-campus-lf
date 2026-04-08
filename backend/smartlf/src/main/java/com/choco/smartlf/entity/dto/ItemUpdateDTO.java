package com.choco.smartlf.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "修改物品信息请求参数")
public class ItemUpdateDTO {

    @NotNull(message = "物品ID不能为空")
    @Schema(description = "要修改的物品主键ID", required = true)
    private Long id;

    @NotBlank(message = "物品核心名称不能为空")
    @Schema(description = "物品核心名称（如：雨伞、钥匙）", example = "雨伞")
    private String itemName;

    @NotNull(message = "发生时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "丢失/拾取发生时间")
    private LocalDateTime eventTime;

    @NotBlank(message = "地点不能为空")
    @Schema(description = "发生地点")
    private String location;

    @NotBlank(message = "帖子标题/简述不能为空")
    @Schema(description = "帖子标题，展示在列表最醒目位置（如：在二饭捡到一把黑色雨伞）", example = "在二饭捡到一把黑色雨伞")
    private String publicDesc;

    @Schema(description = "半公开细节")
    private String semiPublicDesc;

    @Schema(description = "图片URL列表")
    private List<String> imagesUrlList;

    @Schema(description = "核验暗号")
    private String verifyAnswer;

    @Schema(description = "联系方式")
    private String privateContact;
}