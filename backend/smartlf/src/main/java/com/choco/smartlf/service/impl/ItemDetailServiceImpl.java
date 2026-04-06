package com.choco.smartlf.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.choco.smartlf.entity.pojo.ItemDetail;
import com.choco.smartlf.mapper.ItemDetailMapper;
import com.choco.smartlf.service.ItemDetailService;
import org.springframework.stereotype.Service;

/**
* @author renpe
* @description 针对表【item_detail(物品详情表（1对1）)】的数据库操作Service实现
* @createDate 2026-04-06 14:52:41
*/
@Service
public class ItemDetailServiceImpl extends ServiceImpl<ItemDetailMapper, ItemDetail>
    implements ItemDetailService {

}




