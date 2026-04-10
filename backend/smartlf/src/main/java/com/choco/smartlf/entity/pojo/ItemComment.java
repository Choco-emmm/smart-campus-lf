package com.choco.smartlf.entity.pojo;


import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ItemComment {
    @TableId(type = IdType.AUTO)
    @Schema(description="主键ID")
    private Long id;
    @Schema(description="所属帖子ID")
    private Long itemId;
    @Schema(description="评论发布者ID")
    private Long userId;
    @Schema(description="帖主ID (被通知人)")
    private Long targetUserId;
    @Schema(description="留言内容")
    private String content;
    @Schema(description="帖主是否已读 (0:未读, 1:已读)")
    private Integer isRead;
    @Schema(description="留言时间")
    private Date createTime;
}
