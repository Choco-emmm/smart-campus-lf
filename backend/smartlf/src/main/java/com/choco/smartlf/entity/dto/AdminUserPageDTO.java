package com.choco.smartlf.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理员-用户列表分页查询参数")
public class AdminUserPageDTO {

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页记录数", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "搜索关键字 (可匹配用户名、昵称或手机号)")
    private String keyword;

    @Schema(description = "账号状态 (0-正常, 1-封禁)，不传则查全部")
    private Integer status;
}