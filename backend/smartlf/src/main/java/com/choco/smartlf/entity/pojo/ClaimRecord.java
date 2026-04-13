package com.choco.smartlf.entity.pojo;


import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ClaimRecord {
    @TableId(type = IdType.AUTO)
    @Schema(description="认领申请表主键")
    private Long id;
    @Schema(description="帖子ID")
    private Long itemId;
    @Schema(description="发帖人ID")
    private Long publisherId;
    @Schema(description="申请人ID")
    private Long applicantId;
    @Schema(description="申请者填写的暗号答案")
    private String answer;
    @Schema(description=" 0:待审核, 1:同意, 2:拒绝, 3:补充证据 4:补充已提交(等待最终审核)")
    private Integer status;
    @Schema(description="申请时间")
    private LocalDateTime createTime;
    @Schema(description="最后修改时间")
    private LocalDateTime updateTime;
    @Schema(description="发帖人补充的问题")
    private String supplementQuestion;
    @Schema(description="申请人填写的补充答案")
    private String supplementAnswer;
}
