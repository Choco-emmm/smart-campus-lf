package com.choco.smartlf.entity.pojo;


import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
    @Schema(description="物品核心名称(用于分类/搜索，如：雨伞、校园卡)")
    private String itemName;
    @Schema(description="丢失/拾取发生时间")
    private LocalDateTime eventTime;
    @Schema(description="发生地点")
    private String location;
    @Schema(description="帖子标题/公开简述(如：在二饭捡到一把黑色雨伞)")
    private String publicDesc;
    @Schema(description="0:寻找中，1:锁定中，2:已结案,3:违规下架", implementation = com.choco.smartlf.enums.ItemStatusEnum.class)
    private Integer status;
    @Schema(description="是否置顶（0:否，1:是）", implementation = com.choco.smartlf.enums.TopEnum.class)
    private Integer isTop;
    @Schema(description="置顶结束时间")
    private LocalDateTime topEndTime;
    @Schema(description="1:高价值/敏感（强制二审）", implementation = com.choco.smartlf.enums.SensitiveEnum.class)
    private Integer isSensitive;
    @TableLogic
    @Schema(description="逻辑删除（0:否，1:是）", implementation = com.choco.smartlf.enums.DeletedEnum.class)
    private Integer isDeleted;
    @Schema(description="创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    @Schema(description = "最后被回复时间")
    private LocalDateTime latestReplyTime;
}
