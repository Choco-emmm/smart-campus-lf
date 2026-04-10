package com.choco.smartlf.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentAddDTO {
    @NotNull(message = "帖子ID不能为空")
    @Schema(description="帖子ID")
    private Long itemId;
    @NotBlank(message = "留言内容不能为空")
    @Schema(description="留言内容")
    private String content;
}