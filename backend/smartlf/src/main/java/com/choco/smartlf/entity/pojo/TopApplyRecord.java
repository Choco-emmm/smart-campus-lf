package com.choco.smartlf.entity.pojo;


import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TopApplyRecord {
    @TableId(type = IdType.AUTO)
    @Schema(description="申请主键")
    private Long id;
    @Schema(description="申请置顶的物品ID")
    private Long itemId;
    @Schema(description="申请人ID")
    private Long userId;
    @Schema(description="申请理由")
    private String applyReason;
    @Schema(description="0:待审核, 1:已同意, 2:已拒绝")
    private Integer status;
    @Schema(description="申请时间")
    private Date createTime;
    @Schema(description="管理员处理备注")
    private String processRemark;
}
