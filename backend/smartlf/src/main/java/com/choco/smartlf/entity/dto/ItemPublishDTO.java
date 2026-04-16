package com.choco.smartlf.entity.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.choco.smartlf.utils.Constant;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.networknt.schema.format.RegexFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "发布失物/招领请求参数")
public class ItemPublishDTO {

    @NotNull(message = "类型不能为空")
    @Min(value = 0, message = "类型格式不正确")
    @Max(value = 1, message = "类型格式不正确")
    @Schema(description = "0:丢失，1:拾取", required = true,example = "0")
    private Integer type;

    @NotBlank(message = "物品核心名称不能为空")
    @Schema(description = "物品核心名称（如：雨伞、钥匙）", required = true, example = "雨伞")
    private String itemName;

    @NotNull(message = "发生时间不能为空")
    @Schema(description = "丢失/拾取发生时间", required = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime eventTime;

    @NotBlank(message = "地点不能为空")
    @Schema(description = "发生地点", required = true, example = "二教 A304")
    private String location;

    @NotBlank(message = "帖子标题/简述不能为空")
    @Schema(description = "帖子标题，展示在列表最醒目位置（如：在二饭捡到一把黑色雨伞）", required = true,example = "在二饭捡到一把黑色雨伞")
    private String publicDesc;

    @Schema(description = "半公开细节（可为空）")
    private String semiPublicDesc;

    @Schema(description = "图片URL列表（前端上传后拿到的URL数组）")
    private List<String> imagesUrlList;

    @Schema(description = "核验问题（选填，贵重物品填写）")
    private  String verifyQuestion;

    @Schema(description = "核验暗号（选填，贵重物品填写）")
    private String verifyAnswer;


    @Schema(description = "联系方式（选填，贵重物品填写）")
    private String privateContact;
}