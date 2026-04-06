package com.choco.smartlf.entity.pojo;


import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ClaimRecord {
    @Schema(description="申请主键")
    private Long id;
    @Schema(description="申请的物品ID")
    private Long itemId;
    @Schema(description="申请人ID")
    private Long applicantId;
    @Schema(description="申请者填写的暗号答案")
    private String answer;
    @Schema(description="0:待帖主审, 1:待管理员审, 2:通过, 3:拒绝")
    private Integer status;
    @Schema(description="申请时间")
    private Date createTime;
    @Schema(description="最后修改时间")
    private Date updateTime;
}
