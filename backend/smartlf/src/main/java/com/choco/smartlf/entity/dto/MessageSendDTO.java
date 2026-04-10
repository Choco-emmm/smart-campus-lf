package com.choco.smartlf.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageSendDTO {
    @NotNull(message = "接收人ID不能为空")
    @Schema(description="接收人ID")
    private Long receiverId;
    @NotBlank(message = "私信内容不能为空")
    @Schema(description="私信内容")
    private String content;
}