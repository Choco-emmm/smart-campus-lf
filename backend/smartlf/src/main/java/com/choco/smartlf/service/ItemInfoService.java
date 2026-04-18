package com.choco.smartlf.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.choco.smartlf.entity.dto.*;
import com.choco.smartlf.entity.pojo.ItemInfo;
import com.choco.smartlf.entity.vo.AdminItemDetailVO;
import com.choco.smartlf.entity.vo.AdminStatsVO;
import com.choco.smartlf.entity.vo.ItemDetailVO;
import com.choco.smartlf.entity.vo.ItemListVO;
import com.choco.smartlf.service.impl.ItemInfoServiceImpl;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author renpe
* @description 针对表【item_info(物品核心主表)】的数据库操作Service
* @createDate 2026-04-06 14:52:41
*/
public interface ItemInfoService extends IService<ItemInfo> {

    Long publishItem(ItemPublishDTO dto);

    String uploadImage(MultipartFile file);

    ItemDetailVO getItemDetail(Long id);

    void updateItem(ItemUpdateDTO dto);

    void deleteItem(Long id);

    void updateStatus(ItemStatusUpdateDTO dto);

    IPage<ItemListVO> pageQuery(ItemPageQueryDTO dto);

    AdminStatsVO getPlatformStats(LocalDateTime startTime, LocalDateTime endTime);

    IPage<ItemListVO> myPublishPage(Integer pageNum, Integer pageSize);

    void banItemByAdmin(Long itemId);

    AdminItemDetailVO getItemDetailByAdmin(Long itemId);

    ItemInfoServiceImpl.AIExtractResultDTO generateMultimodalInfo(ItemInfo itemInfo, List<String> imageUrls, String userDesc, Long userId);

    void generateAdminSummary();

    ItemUpdateDTO getEditEcho(Long id);

    void toggleTopByAdmin(Long itemId, Integer isTop);

    IPage<ItemListVO> othersPublishPage(Long userId, Integer pageNum, Integer pageSize);

    void deleteFromVector(Long itemId);

}
