package com.choco.smartlf.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClaimSupplementDTO {
    @Schema(description="原认领申请单ID")
    @NotNull
    private Long claimId;
    @Schema(description="补充答案")
    @NotBlank(message = "补充答案不能为空")
    private String supplementAnswer;
}