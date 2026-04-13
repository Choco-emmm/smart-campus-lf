package com.choco.smartlf.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClaimRecordVO {
    @Schema(description="认领申请单ID")
    private Long id;
    @Schema(description="帖子ID")
    private Long itemId;
    @Schema(description="帖子名称")
    private String itemName;
    @Schema(description="申请者ID")
    private Long applicantId;
    @Schema(description="帖子发布者ID")
    private Long publisherId;
    @Schema(description="对方昵称")
    private String targetNickname; // 对方昵称（动态赋值）
    @Schema(description="对方头像")
    private String targetAvatar;   // 对方头像（动态赋值）
    @Schema(description="核验问题")
    private String verifyQuestion;
    @Schema(description="核验答案")
    private String verifyAnswer;
    @Schema(description="申请人自己填的暗号答案")
    private String answer;//动态赋值
    @Schema(description="申请状态， 0:待审核, 1:同意, 2:拒绝, 3:补充证据 4:补充已提交(等待最终审核)")
    private Integer status;
    @Schema(description="取件码，过期会设为\"已过期失效\"")
    private String pickupCode;     // 取件码
    @Schema(description="取件码有效期，过期会设为null")
    private LocalDateTime codeExpireTime;
    @Schema(description="申请时间")
    private LocalDateTime createTime;
    @Schema(description="最后修改时间")
    private LocalDateTime updateTime;
    @Schema(description="发帖人联系方式")
    private String publisherContact; // 同意后展示的联系方式
    @Schema(description="发帖人问的补充问题")
    private String supplementQuestion;
    @Schema(description="申请人填的补充答案")
    private String supplementAnswer;
}