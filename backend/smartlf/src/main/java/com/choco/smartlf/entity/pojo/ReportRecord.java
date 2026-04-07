package com.choco.smartlf.entity.pojo;


import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReportRecord {
    @TableId(type = IdType.AUTO)
    @Schema(description="举报主键")
    private Long id;
    @Schema(description="被举报的物品ID")
    private Long itemId;
    @Schema(description="举报人ID")
    private Long reporterId;
    @Schema(description="举报理由")
    private String reason;
    @Schema(description="0:待处理, 1:核实, 2:驳回")
    private Integer status;
    @Schema(description="")
    private Date createTime;
}
