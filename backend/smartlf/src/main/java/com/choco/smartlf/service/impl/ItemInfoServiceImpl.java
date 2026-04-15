package com.choco.smartlf.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.choco.smartlf.entity.dto.*;
import com.choco.smartlf.entity.pojo.*;
import com.choco.smartlf.entity.vo.AdminItemDetailVO;
import com.choco.smartlf.entity.vo.AdminStatsVO;
import com.choco.smartlf.entity.vo.ItemDetailVO;
import com.choco.smartlf.entity.vo.ItemListVO;
import com.choco.smartlf.enums.*;
import com.choco.smartlf.exception.BusinessException;
import com.choco.smartlf.mapper.ItemInfoMapper;
import com.choco.smartlf.service.*;
import com.choco.smartlf.utils.*;
import com.choco.smartlf.websocket.ChatWebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private final UserActiveLogService userActiveLogService;
    private final ItemInfoMapper itemInfoMapper;
    private final ChatClient polishClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final SimpleVectorStore simpleVectorStore;

    @Autowired
    @Qualifier("aiExecutor")
    private Executor aiExecutor;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long publishItem(ItemPublishDTO dto) {
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


        if (StrUtil.isNotBlank(dto.getVerifyAnswer()) || StrUtil.isNotBlank(dto.getPrivateContact()) || StrUtil.isNotBlank(dto.getVerifyQuestion())) {
            // 兜底校验：核验问题，核验答案，联系方式，三者要么全都没有，要么全都要填
            if (StrUtil.isBlank(dto.getVerifyAnswer()) || StrUtil.isBlank(dto.getPrivateContact()) || StrUtil.isBlank(dto.getVerifyQuestion())) {
                throw new BusinessException("开启私密核验时，问题、答案和联系方式必须同时填写！");
            }
            ItemSecure itemSecure = new ItemSecure();
            itemSecure.setItemId(itemId);
            itemSecure.setVerifyAnswer(dto.getVerifyAnswer());
            itemSecure.setPrivateContact(dto.getPrivateContact());
            //插入核验表
            itemSecureService.save(itemSecure);
        }
        log.info("失物信息发布成功，帖子ID: {}", itemId);

        // 🌟 触发 AI 多模态识别任务
        // 只要用户传了图片，或者写了初步的 publicDesc，我们都可以让 AI 去识别补全
        if (CollUtil.isNotEmpty(dto.getImagesUrlList())
                ||StrUtil.isNotBlank(dto.getPublicDesc())) {
            generateMultimodalInfoAsync(itemInfo, dto.getImagesUrlList(), dto.getPublicDesc(), UserContext.getUserId());
        }
        return itemId;
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
        ItemDetailVO vo = buildBaseItemDetailVO(itemInfo);
        //设置置顶状态
        vo.setIsTop(TopEnum.YES.getCode().equals(itemInfo.getIsTop()));

        //去核验表里查一下有没有这个物品的记录
        long secureCount = itemSecureService.count(
                new LambdaQueryWrapper<ItemSecure>().eq(ItemSecure::getItemId, id)
        );

        //如果 count > 0，说明发帖人存了暗号和联系方式，设置为 true
        vo.setHasSecureCheck(secureCount > 0);

        log.info("失物信息详情查询成功，物品ID: {}", id);
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
        if (StrUtil.isBlank(dto.getVerifyAnswer()) && StrUtil.isBlank(dto.getPrivateContact()) && StrUtil.isBlank(dto.getVerifyQuestion())) {
            // 用户想关闭：直接把核验记录删掉
            itemSecureService.removeById(itemId);
        } else {
            // 用户想开启或修改：必须三者都填
            if (StrUtil.isBlank(dto.getVerifyAnswer()) || StrUtil.isBlank(dto.getPrivateContact()) || StrUtil.isBlank(dto.getVerifyQuestion())) {
                throw new BusinessException("开启私密核验时，问题、答案与联系方式必须同时填写！");
            }
            ItemSecure itemSecure = new ItemSecure();
            itemSecure.setItemId(itemId);
            itemSecure.setVerifyQuestion(dto.getVerifyQuestion());
            itemSecure.setVerifyAnswer(dto.getVerifyAnswer());
            itemSecure.setPrivateContact(dto.getPrivateContact());
            // 使用 saveOrUpdate：如果以前没开启就插入，开启了就更新
            itemSecureService.saveOrUpdate(itemSecure);
        }
        //查询新的info信息
        ItemInfo newItemInfo = this.getById(itemId);
        //调用ai润色+生成分类
        generateMultimodalInfoAsync(newItemInfo, dto.getImagesUrlList(), dto.getPublicDesc(), currentUserId);

        log.info("物品信息更新成功，物品ID: {}, 操作人ID: {}", itemId, currentUserId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteItem(Long id) {
        //安全核验
        Long userId = UserContext.getUserId();
        ItemInfo item = getById(id);
        if (item == null) {
            throw new BusinessException("该物品信息不存在");
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.FORBIDDEN, "你没有权限删除他人的帖子！");
        }
        item.setIsDeleted(DeletedEnum.YES.getCode());
        //将物品信息逻辑删除
        this.removeById(id);
        log.info("物品信息逻辑删除成功，物品ID: {}, 操作人ID: {}", id, userId);
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

        // A. 基础过滤：排除被封禁的
        wrapper.ne(ItemInfo::getStatus, ItemStatusEnum.BANNED.getCode());

        // B. 类型筛选：寻物(0) 或 招领(1)
        // 对应广场顶部的 Tab 切换
        if (dto.getType() != null) {
            wrapper.eq(ItemInfo::getType, dto.getType());
        }

        // C. 状态筛选：寻找中(0)、锁定中(1)、已结案(2)
        if (dto.getStatus() != null) {
            wrapper.eq(ItemInfo::getStatus, dto.getStatus());
        }

        // D. 物品名称/描述搜索（关键字）
        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w.like(ItemInfo::getItemName, dto.getKeyword())
                    .or()
                    .like(ItemInfo::getPublicDesc, dto.getKeyword()));
        }

        // E. 地点筛选
        if (StrUtil.isNotBlank(dto.getLocation())) {
            wrapper.like(ItemInfo::getLocation, dto.getLocation());
        }

        // F. 排序核心：支持“时间排序”与“置顶”
        // 优先级：1. 置顶贴排最前 2. 按最后活跃时间（顶贴时间）倒序排 3. 最后按创建时间保底
        wrapper.orderByDesc(ItemInfo::getIsTop)
                .orderByDesc(ItemInfo::getLatestReplyTime)
                .orderByDesc(ItemInfo::getCreateTime);

        // 3. 执行查询
        this.page(page, wrapper);

        // 4. 数据转换：ItemInfo -> ItemListVO
        return page.convert(itemInfo -> {
            ItemListVO vo = new ItemListVO();
            BeanUtil.copyProperties(itemInfo, vo);

            // 查找封面图
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

    @Override
    public AdminStatsVO getPlatformStats(LocalDateTime startTime, LocalDateTime endTime) {
        // 1. 时间兜底逻辑：保护后端不报空指针
        if (endTime == null) {
            endTime = LocalDateTime.now(); // 默认当前时间
        }
        if (startTime == null) {
            startTime = endTime.minusDays(7); // 默认往前推7天
        }

        AdminStatsVO vo = new AdminStatsVO();

        // 2. 统计总发帖量 (不加条件，直接查 ItemInfo 全表)
        long totalItems = this.count();
        vo.setTotalItems(totalItems);

        // 3. 统计已结案(找回)数量
        long solvedItems = this.count(new LambdaQueryWrapper<ItemInfo>()
                .eq(ItemInfo::getStatus, ItemStatusEnum.CLOSED.getCode()));
        vo.setSolvedItems(solvedItems);


        // 5. 统计指定时间内的活跃用户数 (按 userId 去重)
        // 因为 MyBatis-Plus 的 count() 默认是 SELECT COUNT(*)，无法直接去重
        // 所以我们这里用 groupBy 分组查询来达到去重的效果，获取分组后的集合大小
        long activeUsers = userActiveLogService.list(new LambdaQueryWrapper<UserActiveLog>()
                .select(UserActiveLog::getUserId) // 只查 userId 字段
                .ne(UserActiveLog::getRole, RoleEnum.ADMIN.getCode())//不需要统计管理员身份的活跃数据？
                .ge(UserActiveLog::getActiveTime, startTime) // 大于等于开始时间
                .le(UserActiveLog::getActiveTime, endTime)   // 小于等于结束时间
                .groupBy(UserActiveLog::getUserId) // 按用户ID去重
        ).size();
        vo.setActiveUsers(activeUsers);

        return vo;
    }

    @Override
    public IPage<ItemListVO> myPublishPage(Integer pageNum, Integer pageSize) {
        // 1. 获取当前登录用户 ID
        Long currentUserId = UserContext.getUserId();

        // 2. 构造分页对象和查询条件
        Page<ItemInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ItemInfo> wrapper = new LambdaQueryWrapper<>();

        // 🌟 核心条件：只查自己的
        wrapper.eq(ItemInfo::getUserId, currentUserId);

        // 这里绝不加 status != 3 的条件，能查出违规贴。

        // 按创建时间倒序（最新的在最上面）
        wrapper.orderByDesc(ItemInfo::getCreateTime);

        // 3. 执行查询
        this.page(page, wrapper);

        // 4. 数据转换：ItemInfo -> ItemListVO (和广场的转换逻辑一致，带上封面图)
        return page.convert(itemInfo -> {
            ItemListVO vo = new ItemListVO();
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

    @Override
    public void banItemByAdmin(Long itemId) {
        // 1. 获取失物信息
        ItemInfo item = this.getById(itemId);
        if (item == null) {
            throw new BusinessException("该失物信息不存在");
        }
        if (item.getStatus().equals(ItemStatusEnum.BANNED.getCode())) {
            throw new BusinessException("该失物信息已违规下架，请勿重复封禁");
        }
        item.setStatus(ItemStatusEnum.BANNED.getCode());
        updateById(item);
        log.info("物品ID {} 已被封禁", item.getId());
    }

    @Override
    public AdminItemDetailVO getItemDetailByAdmin(Long id) {
        // 使用我们上一回合手写的 Mapper，无视逻辑删除强制查出底表数据！
        ItemInfo itemInfo = itemInfoMapper.getByIdForAdmin(id);
        ItemDetailVO baseVo = buildBaseItemDetailVO(itemInfo);

        // 将基础 VO 转换为 Admin 专属 VO
        AdminItemDetailVO adminVo = new AdminItemDetailVO();
        BeanUtil.copyProperties(baseVo, adminVo);

        // 去核验表里把【真实的明细数据】查出来！
        ItemSecure secure = itemSecureService.getOne(
                new LambdaQueryWrapper<ItemSecure>().eq(ItemSecure::getItemId, id)
        );

        if (secure != null) {
            adminVo.setHasSecureCheck(true);
            // 塞入上帝视角的敏感数据
            adminVo.setCheckQuestion(secure.getVerifyQuestion());
            adminVo.setCheckAnswer(secure.getVerifyAnswer());
            adminVo.setContactInfo(secure.getPrivateContact());
        } else {
            adminVo.setHasSecureCheck(false);
        }

        log.info("管理员查询详情成功，物品ID: {}", id);
        return adminVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateAIDesc(Long itemId) {
        // 🌟 1. 主线程：在把活儿交给异步线程前，赶紧把当前用户的 ID 拿出来存好！
        // （防止异步线程里拿不到 UserContext 导致空指针）
        Long currentUserId = UserContext.getUserId();

        // 校验物品和权限
        ItemInfo item = this.getById(itemId);
        if (item == null) {
            throw new BusinessException("物品不存在");
        }
        if (!item.getUserId().equals(currentUserId)) {
            throw new BusinessException(ResultCodeEnum.FORBIDDEN, "只能让AI润色自己的帖子！");
        }

        // 构建给 AI 的提示词
        String typeStr = Objects.equals(item.getType(), ItemTypeEnum.LOST.getCode()) ? ItemTypeEnum.LOST.getDescription() : ItemTypeEnum.FOUND.getDescription();
        String promptText = String.format(
                AIConstant.PROMPT_TEMPLATE_POLISH,
                typeStr, item.getItemName(), item.getLocation(), item.getPublicDesc()
        );

        log.info("【主线程】已接收 AI 润色请求，提交至后台排队处理。物品ID: {}", itemId);

        // 🌟 2. 异步：将耗时的调用丢给 aiExecutor 线程池
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                log.info("【异步线程】开始调用本地大模型生成描述...");
                String aiResult = polishClient.prompt()
                        .user(promptText)
                        .call()
                        .content();

                if (StrUtil.isNotBlank(aiResult)) {
                    // 3. 落库：将结果覆盖到旧数据上
                    ItemDetail itemDetail = itemDetailService.getById(itemId);
                    if (itemDetail != null) {
                        itemDetail.setAiGeneratedDesc(aiResult);
                        itemDetailService.updateById(itemDetail);
                        log.info("【异步线程】AI 描述生成并保存成功，物品ID: {}", itemId);

                    }
                }
            } catch (Exception e) {
                log.error("【异步线程】AI 描述生成彻底失败，物品ID: {}", itemId, e);
            }
        }, aiExecutor);

        // 主线程走到这里（耗时不到 1 毫秒）直接结束，瞬间返回给 Controller！
    }

    /**
     * 核心：异步多模态视觉识别（多图 + 全文本融合）
     * @param itemInfo 刚刚落库完毕的完整物品信息对象
     * @param imageUrls 前端传来的图片 URL 数组
     * @param userDesc 用户填写的初步描述 (PublicDesc)
     * @param userId 触发该操作的用户ID (用于WebSocket回调通知)
     */
    public void generateMultimodalInfoAsync(ItemInfo itemInfo, List<String> imageUrls, String userDesc, Long userId) {
        Long itemId = itemInfo.getId();
        log.info("【主线程】触发多模态全维度识别任务，物品ID: {}", itemId);

        CompletableFuture.runAsync(() -> {
            try {
                log.info("【异步线程】开始调用 Qwen3.5-Vision 模型...");

                // 1. 组装多张图片的 Media 列表 (跟你之前的代码一样)
               List<Media> mediaList = new ArrayList<>();
                if (imageUrls != null && !imageUrls.isEmpty()) {
                    for (String url : imageUrls) {
                        String relativePath = url.replaceFirst(Constant.IMAGE_PATH_PREFIX_REGEX, "");
                        String physicalPath = filePrefix + relativePath;
                        File imageFile = new File(physicalPath);

                        if (imageFile.exists()) {
                           FileSystemResource resource = new FileSystemResource(imageFile);
                            mediaList.add(new Media(
                                    MimeTypeUtils.IMAGE_JPEG, resource));
                        }
                    }
                }

                if (mediaList.isEmpty() && StrUtil.isBlank(userDesc)) {
                    return; // 无图无描述，终止
                }

                // 2. 🌟 提取 ItemInfo 中的高价值字段给 AI！
                String typeStr = itemInfo.getType().equals(ItemTypeEnum.LOST.getCode()) ? "寻物" : "拾物招领";
                String safeName = StrUtil.isBlank(itemInfo.getItemName()) ? "未知物品" : itemInfo.getItemName();
                String safeTitle= StrUtil.isBlank(itemInfo.getPublicDesc()) ? "无标题" : itemInfo.getPublicDesc();
                String safeLocation = StrUtil.isBlank(itemInfo.getLocation()) ? "未知地点" : itemInfo.getLocation();
                String safeDesc = StrUtil.isBlank(userDesc) ? "无附加描述" : userDesc;

                // 🌟 新增：安全提取时间并格式化
                // 注意：这里的 getTime() 请换成你 ItemInfo 实体类里实际的时间字段名（比如 getHappenTime 或 getCreateTime）
                String safeTime = "未知时间";
                if (itemInfo.getEventTime() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    safeTime = itemInfo.getEventTime().format(formatter);
                }

                // 🌟 组装 Prompt：按顺序传入 5 个参数！
                String promptText = String.format(AIConstant.MULTIMODAL_EXTRACT_PROMPT,
                        typeStr,safeTitle, safeName, safeLocation, safeTime, safeDesc);

                log.info("【异步线程】多模态识别 Prompt：{}", promptText);

                // 3. 构建多模态请求
                 // Spring AI 的 .user() 方法支持传入 Object... 可变参数
                String aiResponseStr = polishClient.prompt()
                        .user(u -> u.text(promptText).media(mediaList.toArray(new Media[0])))
                        .call()
                        .content();
                log.info("【异步线程】多模态识别结果：{}", aiResponseStr);
                // 5. 🛡️ 防御性解析并落库
                String cleanJson = aiResponseStr.replace("```json", "").replace("```", "").trim();
                AIExtractResultDTO aiResult = JSONUtil.toBean(cleanJson, AIExtractResultDTO.class);

                if (aiResult != null) {
                    if (StrUtil.isNotBlank(aiResult.getAiCategory())) {
                        ItemInfo infoUpdate = new ItemInfo();
                        infoUpdate.setId(itemId);
                        infoUpdate.setAiCategory(StrUtil.subPre(aiResult.getAiCategory(), 50));
                        itemInfoMapper.updateById(infoUpdate);
                    }

                    if (StrUtil.isNotBlank(aiResult.getAiGeneratedDesc())) {
                        ItemDetail detailUpdate = new ItemDetail();
                        detailUpdate.setItemId(itemId);
                        detailUpdate.setAiGeneratedDesc(StrUtil.subPre(aiResult.getAiGeneratedDesc(), 500));
                        itemDetailService.updateById(detailUpdate);
                    }
                    log.info("【异步线程】多模态融合落库成功！物品ID: {}", itemId);

                    String vectorMsg = String.format(AIConstant.VECTOR_FORMATTER, typeStr,safeTitle, safeName, safeLocation, safeTime, safeDesc,aiResult.getAiCategory(), aiResult.getAiGeneratedDesc());
                    // 1. 拼接Nomic 模型专属的被动搜索前缀,原有内容和新增内容
                    String finalContent = StrUtil.builder().append(AIConstant.NOMIC_DOC_PREFIX).append(vectorMsg).toString();
                    log.info("【向量库】向量库写入内容：{}", finalContent);
                    // 3. 打包成带元数据的 Document
                    Document document = new Document(finalContent, Map.of("itemId", itemId));

                    // 4. 向量化并加入内存
                    simpleVectorStore.add(List.of(document));

                    // 5. 🌟 核心：立刻触发本地化存档！
                    simpleVectorStore.save(new File(AIConstant.VECTOR_STORE_FILE_PATH));
                    log.info("【向量库】帖子 {} 已成功存入并同步至本地文件！", itemId);

                    log.info("【异步线程】向量知识库灌入完成！帖子ID: {}", itemId);

                    // 6. 🌟 WebSocket 通知用户
                    String noticeMsg = String.format(WsNoticeConstant.AI_POLISH_FINISH, safeName);
                    ChatWebSocketServer.pushSystemNotice(userId, noticeMsg);
                }

            } catch (JSONException e) {
                log.error("【异步线程】AI 返回非合法 JSON，解析失败！");
            } catch (Exception e) {
                log.error("【异步线程】AI 多模态任务执行异常", e);
            }
        }, aiExecutor);
    }

    @Override
    public void generateAdminSummary() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<ItemInfo> recentItems = this.list(new LambdaQueryWrapper<ItemInfo>()
                .ge(ItemInfo::getCreateTime, sevenDaysAgo));

        if (recentItems.isEmpty()) {
            return;
        }

        Map<String, Long> nameCountMap = recentItems.stream()
                .collect(Collectors.groupingBy(ItemInfo::getItemName, Collectors.counting()));
        Map<String, Long> locationCountMap = recentItems.stream()
                .collect(Collectors.groupingBy(ItemInfo::getLocation, Collectors.counting()));

        String rawDataStr = String.format(AIConstant.RAW_DATA_TEMPLATE, recentItems.size(), nameCountMap.toString(), locationCountMap.toString());
        String promptText = AIConstant.getAdminSummaryPrompt(rawDataStr);

        log.info("【主线程】准备提交管理员 AI 周报任务...");

        // 🌟 同样采用异步化处理，防止卡死定时任务调度器
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                String content = polishClient.prompt()
                        .user(promptText)
                        .call()
                        .content();

                if (content != null) {
                    stringRedisTemplate.opsForValue().set(AIConstant.SUMMARY_KEY, content, AIConstant.SUMMARY_EXPIRE_DAYS, java.util.concurrent.TimeUnit.DAYS);
                    log.info("【异步线程】AI 本周安保报告已生成并存入 Redis！");
                }
            } catch (Exception e) {
                log.error("【异步线程】AI 报告生成失败", e);
            }
        }, aiExecutor);
    }

    @Override
    public ItemUpdateDTO getEditEcho(Long id) {
        Long currentUserId = UserContext.getUserId();

        // 1. 查主表并做严格越权校验
        ItemInfo itemInfo = this.getById(id);
        if (itemInfo == null) {
            throw new BusinessException("该物品信息不存在");
        }
        if (!itemInfo.getUserId().equals(currentUserId)) {
            throw new BusinessException(ResultCodeEnum.FORBIDDEN, "越权访问：你没有权限查看他人的敏感编辑信息！");
        }

        // 2. 初始化前端要的 DTO，把主表数据拷进去
        ItemUpdateDTO dto = new ItemUpdateDTO();
        BeanUtil.copyProperties(itemInfo, dto);

        // 3. 查详情表，补充半公开细节和图片
        ItemDetail itemDetail = itemDetailService.getById(id);
        if (itemDetail != null) {
            dto.setSemiPublicDesc(itemDetail.getSemiPublicDesc());
            if (StrUtil.isNotBlank(itemDetail.getImagesUrl())) {
                dto.setImagesUrlList(JSONUtil.toList(itemDetail.getImagesUrl(), String.class));
            }
        }

        // 4. 🌟 最关键的一步：查核验表，把真实的问题，暗号和联系方式查出来给前端！
        ItemSecure itemSecure = itemSecureService.getOne(
                new LambdaQueryWrapper<ItemSecure>().eq(ItemSecure::getItemId, id)
        );
        if (itemSecure != null) {

            dto.setVerifyAnswer(itemSecure.getVerifyAnswer());
            dto.setPrivateContact(itemSecure.getPrivateContact());
            dto.setVerifyQuestion(itemSecure.getVerifyQuestion());
        }

        log.info("回显数据成功，物品ID: {}, 操作人: {}", id, currentUserId);
        return dto;
    }

    @Override
    public void toggleTopByAdmin(Long itemId, Integer isTop) {
        //查询失物信息
        ItemInfo itemInfo = this.getById(itemId);
        if (itemInfo == null) {
            throw new BusinessException("该失物信息不存在");
        }

        if (isTop.equals(TopEnum.YES.getCode())) {
            // 1. 如果 isTop == 1，将 is_top 设为 1，并设置 top_end_time（24小时）
            itemInfo.setIsTop(TopEnum.YES.getCode());
            itemInfo.setTopEndTime(LocalDateTime.now().plusHours(Constant.TOP_END_TIME_HOURS));
        } else if (isTop.equals(TopEnum.NO.getCode())) {
            // 2. 如果 isTop == 0，将 is_top 设为 0，清空 top_end_time
            itemInfo.setIsTop(TopEnum.NO.getCode());
        }else {
            throw new BusinessException("参数错误");
        }

        //更新数据
       this.updateById(itemInfo);
        log.info("物品ID: {} 的置顶状态已更新为: {}", itemId, isTop);
    }

    @Override
    public IPage<ItemListVO> othersPublishPage(Long userId, Integer pageNum, Integer pageSize) {
        // 1. 构造分页对象和查询条件
        Page<ItemInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ItemInfo> wrapper = new LambdaQueryWrapper<>();

        // 🌟 核心条件：查目标用户的发布
        wrapper.eq(ItemInfo::getUserId, userId);

        // 🌟 核心拦截：绝不能查出违规下架的帖子 (status != 3)
        wrapper.ne(ItemInfo::getStatus, ItemStatusEnum.BANNED.getCode());

        // 按创建时间倒序（最新的在最上面）
        wrapper.orderByDesc(ItemInfo::getCreateTime);

        // 3. 执行查询
        this.page(page, wrapper);

        // 4. 数据转换：ItemInfo -> ItemListVO (复用已有的转换逻辑，带上封面图)
        return page.convert(itemInfo -> {
            ItemListVO vo = new ItemListVO();
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

    private ItemDetailVO buildBaseItemDetailVO(ItemInfo itemInfo) {
        if (itemInfo == null) {
            throw new BusinessException("该失物信息不存在");
        }

        Long id = itemInfo.getId();
        ItemDetail itemDetail = itemDetailService.getById(id);

        // 组装 VO
        ItemDetailVO vo = new ItemDetailVO();
        BeanUtil.copyProperties(itemInfo, vo);

        if (itemDetail != null) {
            // 有详情信息
            vo.setSemiPublicDesc(itemDetail.getSemiPublicDesc());
            if (StrUtil.isNotBlank(itemDetail.getImagesUrl())) {
                vo.setImagesUrlList(JSONUtil.toList(itemDetail.getImagesUrl(), String.class));
            }
            vo.setAiGeneratedDesc(itemDetail.getAiGeneratedDesc());
        }

        // 获取发布者信息
        User publisher = userService.getById(itemInfo.getUserId());
        if (publisher != null) {
            vo.setPublisherNickname(publisher.getNickname());
            vo.setAvatarUrl(publisher.getAvatarUrl());
        } else {
            vo.setPublisherNickname("用户已注销");
            // 默认头像在前端再设置
        }
        return vo;
    }
}




