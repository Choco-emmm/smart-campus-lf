package com.choco.smartlf.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.choco.smartlf.entity.pojo.ItemInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


/**
* @author Choco
* @description 针对表【item_info(物品核心主表)】的数据库操作Mapper
* @createDate 2026-04-06 14:52:41
* @Entity .com.choco.smartlf.entity.po.ItemInfo
*/
public interface ItemInfoMapper extends BaseMapper<ItemInfo> {
    @Select("SELECT * FROM item_info WHERE id = #{id}")
    ItemInfo getByIdForAdmin(@Param("id") Long id);

}




