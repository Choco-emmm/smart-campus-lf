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
    private Integer role; // 0:学生, 1:管理员
    private LocalDateTime createTime;
}