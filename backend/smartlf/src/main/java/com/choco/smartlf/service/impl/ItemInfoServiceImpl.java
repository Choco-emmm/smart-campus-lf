package com.choco.smartlf.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.choco.smartlf.entity.dto.*;
import com.choco.smartlf.entity.pojo.ItemDetail;
import com.choco.smartlf.entity.pojo.ItemInfo;
import com.choco.smartlf.entity.pojo.ItemSecure;
import com.choco.smartlf.entity.pojo.User;
import com.choco.smartlf.entity.vo.ItemDetailVO;
import com.choco.smartlf.entity.vo.ItemListVO;
import com.choco.smartlf.enums.ItemStatusEnum;
import com.choco.smartlf.enums.ResultCodeEnum;
import com.choco.smartlf.enums.TopEnum;
import com.choco.smartlf.exception.BusinessException;
import com.choco.smartlf.mapper.ItemInfoMapper;
import com.choco.smartlf.service.ItemDetailService;
import com.choco.smartlf.service.ItemInfoService;
import com.choco.smartlf.service.ItemSecureService;
import com.choco.smartlf.service.UserService;
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
    private final UserService userService;

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


        if (StrUtil.isNotBlank(dto.getVerifyAnswer()) || StrUtil.isNotBlank(dto.getPrivateContact())) {
            // 兜底校验：如果填了暗号没填联系方式，或者填了联系方式没填暗号，都要拦截
            if (StrUtil.isBlank(dto.getVerifyAnswer()) || StrUtil.isBlank(dto.getPrivateContact())) {
                throw new BusinessException("开启私密核验时，暗号与联系方式必须同时填写！");
            }
            ItemSecure itemSecure = new ItemSecure();
            itemSecure.setItemId(itemId);
            itemSecure.setVerifyAnswer(dto.getVerifyAnswer());
            itemSecure.setPrivateContact(dto.getPrivateContact());
            //插入核验表
            itemSecureService.save(itemSecure);
        }
        log.info("物品发布成功，物品ID: {}", itemId);
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

    @Override
    public ItemDetailVO getItemDetail(Long id) {
        //查主表和详情表
        ItemInfo itemInfo = this.getById(id);
        if (itemInfo == null) {
            throw new BusinessException("该物品信息不存在或已被删除");
        }
        ItemDetail itemDetail = itemDetailService.getById(id);

        //组装 VO
        ItemDetailVO vo = new ItemDetailVO();
        BeanUtil.copyProperties(itemInfo, vo);
        if (itemDetail != null) {
            //有详情信息
            vo.setSemiPublicDesc(itemDetail.getSemiPublicDesc());
            if (StrUtil.isNotBlank(itemDetail.getImagesUrl())) {
                vo.setImagesUrlList(JSONUtil.toList(itemDetail.getImagesUrl(), String.class));
            }
            vo.setAiGeneratedDesc(itemDetail.getAiGeneratedDesc());
        }

        //去核验表里查一下有没有这个物品的记录
        long secureCount = itemSecureService.count(
                new LambdaQueryWrapper<ItemSecure>().eq(ItemSecure::getItemId, id)
        );

        //如果 count > 0，说明发帖人存了暗号和联系方式，设置为 true
        vo.setHasSecureCheck(secureCount > 0);

        //获取发布者信息
        User publisher = userService.getById(itemInfo.getUserId());
        if (publisher != null) {
            vo.setPublisherNickname(publisher.getNickname());
            vo.setPublisherAvatarUrl(publisher.getAvatarUrl());
        } else {
            vo.setPublisherNickname("用户已注销");
            //默认头像在前端再设置，给所有url为null的都设置
        }

        log.info("物品详情查询成功，物品ID: {}", id);
        return vo;
    }

    @Override
    public void updateItem(ItemUpdateDTO dto) {
        //更新三张表
        Long itemId = dto.getId();
        Long currentUserId = UserContext.getUserId();

        // 1. 校验该物品是否存在，以及是否为当前用户发布的
        ItemInfo oldItem = this.getById(itemId);
        if (oldItem == null) {
            throw new BusinessException("该物品信息不存在");
        }
        if (!oldItem.getUserId().equals(currentUserId)) {
            //只有本人能修改信息，管理员都不行
            throw new BusinessException(ResultCodeEnum.FORBIDDEN, "你没有权限修改他人的帖子！");
        }

        // 2. 更新主表
        ItemInfo itemInfo = new ItemInfo();
        BeanUtil.copyProperties(dto, itemInfo);
        this.updateById(itemInfo);

        // 3. 更新详情表
        ItemDetail itemDetail = new ItemDetail();
        itemDetail.setItemId(itemId);
        itemDetail.setSemiPublicDesc(dto.getSemiPublicDesc());
        if (CollUtil.isNotEmpty(dto.getImagesUrlList())) {
            itemDetail.setImagesUrl(JSONUtil.toJsonStr(dto.getImagesUrlList()));
        } else {
            itemDetail.setImagesUrl(null); // 如果前端传空，说明删除了所有图片
        }
        itemDetailService.updateById(itemDetail);

        // 4. 更新核验表
        // 先看用户是不是想关闭核验模式（把暗号和联系方式都置空）
        if (StrUtil.isBlank(dto.getVerifyAnswer()) && StrUtil.isBlank(dto.getPrivateContact())) {
            // 用户想关闭：直接把核验记录删掉
            itemSecureService.removeById(itemId);
        } else {
            // 用户想开启或修改：必须两者都填
            if (StrUtil.isBlank(dto.getVerifyAnswer()) || StrUtil.isBlank(dto.getPrivateContact())) {
                throw new BusinessException("开启私密核验时，暗号与联系方式必须同时填写！");
            }
            ItemSecure itemSecure = new ItemSecure();
            itemSecure.setItemId(itemId);
            itemSecure.setVerifyAnswer(dto.getVerifyAnswer());
            itemSecure.setPrivateContact(dto.getPrivateContact());
            // 使用 saveOrUpdate：如果以前没开启就插入，开启了就更新
            itemSecureService.saveOrUpdate(itemSecure);
        }

        log.info("物品信息更新成功，物品ID: {}, 操作人ID: {}", itemId, currentUserId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteItem(Long id) {
        //安全核验
        Long userId = UserContext.getUserId();
        ItemInfo item = getById(id);
        if(item==null){
            throw new BusinessException("该物品信息不存在");
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.FORBIDDEN, "你没有权限删除他人的帖子！");
        }
        //删除核验表信息
        itemSecureService.removeById(id);
        //删除详情表信息
        itemDetailService.removeById(id);
        //删除主表信息
        removeById(id);

        log.info("物品物理删除成功，彻底清空三表数据，物品ID: {}, 操作人ID: {}", id, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(ItemStatusUpdateDTO dto) {
        Long currentUserId = UserContext.getUserId();

        // 1. 安全校验：确认物品存在且是本人发布
        ItemInfo item = this.getById(dto.getId());
        if (item == null) {
            throw new BusinessException("该物品信息不存在");
        }
        if (!item.getUserId().equals(currentUserId)) {
            throw new BusinessException(ResultCodeEnum.FORBIDDEN, "你没有权限修改此帖子状态！");
        }

        // 2. 联动逻辑：如果状态改为“已结案(2)”，自动取消置顶
        // 这里的2 对应 ItemStatusEnum 中的定义
        if (dto.getStatus().equals(ItemStatusEnum.CLOSED.getCode())) {

            // 只有当它目前是置顶状态时才需要修改，减少不必要的数据库写操作
            if (item.getIsTop().equals(TopEnum.YES.getCode())) {
                item.setIsTop(TopEnum.NO.getCode());
                item.setTopEndTime(null); // 清空结束时间
                log.info("物品状态变为非活跃，自动取消置顶。物品ID: {}", item.getId());
            }
        }

        // 3. 执行状态更新
        item.setStatus(dto.getStatus());
        this.updateById(item);
    }

    @Override
    public IPage<ItemListVO> pageQuery(ItemPageQueryDTO dto) {
        // 1. 构造分页对象
        Page<ItemInfo> page = new Page<>(dto.getPage(), dto.getPageSize());

        // 2. 构造查询条件
        LambdaQueryWrapper<ItemInfo> wrapper = new LambdaQueryWrapper<>();

        // 防线：绝对不能把违规下架的帖子展示出来
        wrapper.ne(ItemInfo::getStatus, ItemStatusEnum.BANNED.getCode());

        // 动态条件过滤
        wrapper.eq(dto.getType() != null, ItemInfo::getType, dto.getType());
        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w.like(ItemInfo::getItemName, dto.getKeyword())
                    .or()
                    .like(ItemInfo::getPublicDesc, dto.getKeyword()));
        }

        // 排序核心：is_top 倒序 (1 排前面)，然后按创建时间倒序
        wrapper.orderByDesc(ItemInfo::getIsTop).orderByDesc(ItemInfo::getCreateTime);

        // 3. 执行查询
        this.page(page, wrapper);

        // 4. 数据转换：ItemInfo -> ItemListVO
        return page.convert(itemInfo -> {
            ItemListVO vo = new ItemListVO();
            //能转的先转了
            BeanUtil.copyProperties(itemInfo, vo);

            // 去详情表拿第一张图片当封面
            ItemDetail detail = itemDetailService.getById(itemInfo.getId());
            if (detail != null && StrUtil.isNotBlank(detail.getImagesUrl())) {
                List<String> images = JSONUtil.toList(detail.getImagesUrl(), String.class);
                if (CollUtil.isNotEmpty(images)) {
                    vo.setCoverImage(images.getFirst());
                }
            }
            return vo;
        });
    }
}




