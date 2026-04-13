package com.choco.smartlf.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClaimAuditDTO {
    @Schema(description="要审核的认领申请的ID")
    @NotNull
    private Long claimId;
    @Schema(description="审核结果,1:同意, 2:拒绝, 3:要求补充")
    @NotNull
    private Integer status;
    // 🌟 新增：如果 status 是 3，这个字段必须有值
    @Schema(description="补充问题")
    private String supplementQuestion; 
}