package com.choco.smartlf.entity.pojo;


import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ItemInfo {
    @TableId(type = IdType.AUTO)
    @Schema(description="物品主键")
    private Long id;
    @Schema(description="发布者用户ID")
    private Long userId;
    @Schema(description="0:丢失，1:拾取", implementation = com.choco.smartlf.enums.ItemTypeEnum.class)
    private Integer type;
    @Schema(description="物品标题")
    private String itemName;
    @Schema(description="丢失/拾取发生时间")
    private Date eventTime;
    @Schema(description="发生地点")
    private String location;
    @Schema(description="公开可见的简述")
    private String publicDesc;
    @Schema(description="0:寻找中，1:锁定中，2:已结案", implementation = com.choco.smartlf.enums.ItemStatusEnum.class)
    private Integer status;
    @Schema(description="是否置顶（0:否，1:是）", implementation = com.choco.smartlf.enums.TopEnum.class)
    private Integer isTop;
    @Schema(description="置顶结束时间")
    private Date topEndTime;
    @Schema(description="1:高价值/敏感（强制二审）", implementation = com.choco.smartlf.enums.SensitiveEnum.class)
    private Integer isSensitive;
    @Schema(description="被举报次数")
    private Integer reportCount;
    @Schema(description="创建时间")
    private Date createTime;
}
