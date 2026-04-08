package com.choco.smartlf.entity.pojo;


import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserActiveLog {
    @TableId(type = IdType.AUTO)
    @Schema(description="流水主键")
    private Long id;
    @Schema(description="活跃用户ID")
    private Long userId;
    @Schema(description="具体活跃时间点（B+树索引）")
    private LocalDateTime activeTime;
}
