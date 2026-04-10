package com.choco.smartlf.entity.pojo;


import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PrivateMessage {
    @TableId(type = IdType.AUTO)
    @Schema(description="主键ID")
    private Long id;
    @Schema(description="发送方ID")
    private Long senderId;
    @Schema(description="接收方ID")
    private Long receiverId;
    @Schema(description="私信内容")
    private String content;
    @Schema(description="接收方是否已读 (0:未读, 1:已读)")
    private Integer isRead;
    @Schema(description="发送时间")
    private LocalDateTime createTime;
}
