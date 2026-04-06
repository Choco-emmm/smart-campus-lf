package service.impl;

import entity.po.ItemDetail;
import mapper.ItemDetailMapper;
import service.IItemDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 物品详情表（1对1） 服务实现类
 * </p>
 *
 * @author 
 * @since 2026-04-06
 */
@Service
public class ItemDetailServiceImpl extends ServiceImpl<ItemDetailMapper, ItemDetail> implements IItemDetailService {

}
