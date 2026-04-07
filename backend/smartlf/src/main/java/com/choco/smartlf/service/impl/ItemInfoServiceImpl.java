package com.choco.smartlf.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.choco.smartlf.entity.dto.ItemPublishDTO;
import com.choco.smartlf.entity.pojo.ItemDetail;
import com.choco.smartlf.entity.pojo.ItemInfo;
import com.choco.smartlf.entity.pojo.ItemSecure;
import com.choco.smartlf.enums.ResultCodeEnum;
import com.choco.smartlf.exception.BusinessException;
import com.choco.smartlf.mapper.ItemInfoMapper;
import com.choco.smartlf.service.ItemDetailService;
import com.choco.smartlf.service.ItemInfoService;
import com.choco.smartlf.service.ItemSecureService;
import com.choco.smartlf.utils.ImageNameUtil;
import com.choco.smartlf.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author renpe
 * @description 针对表【item_info(物品核心主表)】的数据库操作Service实现
 * @createDate 2026-04-06 14:52:41
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemInfoServiceImpl extends ServiceImpl<ItemInfoMapper, ItemInfo>
        implements ItemInfoService {

    @Value("${system.file-prefix:D:/lfFile/}")
    private String filePrefix;

    private final ItemDetailService itemDetailService;
    private final ItemSecureService itemSecureService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void publishItem(ItemPublishDTO dto) {
        //将传来的数据存入主表
        ItemInfo itemInfo = new ItemInfo();
        BeanUtil.copyProperties(dto, itemInfo);
        itemInfo.setUserId(UserContext.getUserId());
        save(itemInfo);
        log.info("itemInfo: {}", itemInfo);

        //拿到主表的id
        Long itemId = itemInfo.getId();
        log.info("itemId: {}", itemId);
        ItemDetail itemDetail = new ItemDetail();
        itemDetail.setItemId(itemId);
        itemDetail.setSemiPublicDesc(dto.getSemiPublicDesc());
        //把List转为JSON格式的数组
        List<String> imagesUrlList = dto.getImagesUrlList();
        if (CollUtil.isNotEmpty(dto.getImagesUrlList())) {
            //存在图片的话才处理
            itemDetail.setImagesUrl(JSONUtil.toJsonStr(dto.getImagesUrlList()));
        }
        //插入详情表
        itemDetailService.save(itemDetail);
        log.info("itemDetail: {}", itemDetail);


        ItemSecure itemSecure = new ItemSecure();
        itemSecure.setItemId(itemId); // 同样关联主表 ID
        itemSecure.setVerifyAnswer(dto.getVerifyAnswer());
        itemSecure.setPrivateContact(dto.getPrivateContact());
        //插入安全核验表
        itemSecureService.save(itemSecure);

        log.info("物品发布成功，成功落盘三张表，物品ID: {}", itemId);
    }

    @Override
    public String uploadImage(MultipartFile file) {
        // 1. 基础校验
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCodeEnum.INVALID_FILE);
        }

        log.info("开始上传物品图片，原始文件名: {}, 大小: {} bytes",
                file.getOriginalFilename(), file.getSize());

        // 2. 生成唯一文件名 (复用你之前写的 ImageNameUtil)
        String imageName = ImageNameUtil.getImageName(file.getOriginalFilename());

        // 3. 拼接物理存储路径 (专门建一个 items 文件夹来存物品图片，和头像隔离)
        String fullSavePath = filePrefix + "items/" + imageName;
        java.io.File destFile = new java.io.File(fullSavePath);

        // 4. 确保父目录存在
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }

        // 5. 将文件存入本地磁盘
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            log.error("物品图片物理保存失败！目标路径: {}", fullSavePath, e);
            throw new BusinessException(ResultCodeEnum.FILE_UPLOAD_ERROR);
        }

        // 6. 拼接网络访问 URL (对应你 WebConfig 里配置的虚拟路径映射)
        String accessUrl = "/images/items/" + imageName;
        log.info("物品图片上传成功，访问路径: {}", accessUrl);

        return accessUrl;
    }
}




