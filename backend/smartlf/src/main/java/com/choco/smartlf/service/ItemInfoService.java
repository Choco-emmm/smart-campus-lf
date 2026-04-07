package com.choco.smartlf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.choco.smartlf.entity.dto.ItemPublishDTO;
import com.choco.smartlf.entity.pojo.ItemInfo;
import com.choco.smartlf.entity.vo.ItemDetailVO;
import org.springframework.web.multipart.MultipartFile;

/**
* @author renpe
* @description 针对表【item_info(物品核心主表)】的数据库操作Service
* @createDate 2026-04-06 14:52:41
*/
public interface ItemInfoService extends IService<ItemInfo> {

    void publishItem(ItemPublishDTO dto);

    String uploadImage(MultipartFile file);

    ItemDetailVO getItemDetail(Long id);
}
