package service.impl;

import entity.po.ItemInfo;
import mapper.ItemInfoMapper;
import service.IItemInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 物品核心主表 服务实现类
 * </p>
 *
 * @author 
 * @since 2026-04-06
 */
@Service
public class ItemInfoServiceImpl extends ServiceImpl<ItemInfoMapper, ItemInfo> implements IItemInfoService {

}
