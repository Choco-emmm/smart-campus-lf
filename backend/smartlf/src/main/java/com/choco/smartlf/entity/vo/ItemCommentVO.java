package com.choco.smartlf.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemCommentVO {
    private Long id;
    private Long userId;
    private String nickname; // 留言人昵称
    private String avatarUrl; // 留言人头像
    private String content;
    private LocalDateTime createTime;
}