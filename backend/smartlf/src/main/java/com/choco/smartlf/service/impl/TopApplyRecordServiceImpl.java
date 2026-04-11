package com.choco.smartlf.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.choco.smartlf.entity.dto.AdminTopPageDTO;
import com.choco.smartlf.entity.dto.AdminTopResolveDTO;
import com.choco.smartlf.entity.dto.ItemTopApplyDTO;
import com.choco.smartlf.entity.pojo.ItemInfo;
import com.choco.smartlf.entity.pojo.TopApplyRecord;
import com.choco.smartlf.enums.AdminAuditActionEnum;
import com.choco.smartlf.enums.ResultCodeEnum;
import com.choco.smartlf.enums.TopApplyStatusEnum;
import com.choco.smartlf.enums.TopEnum;
import com.choco.smartlf.exception.BusinessException;
import com.choco.smartlf.service.ItemInfoService;
import com.choco.smartlf.service.TopApplyRecordService;
import com.choco.smartlf.mapper.TopApplyRecordMapper;
import com.choco.smartlf.utils.Constant;
import com.choco.smartlf.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @author renpe
 * @description 针对表【top_apply_record(置顶申请记录表)】的数据库操作Service实现
 * @createDate 2026-04-08 09:45:44
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TopApplyRecordServiceImpl extends ServiceImpl<TopApplyRecordMapper, TopApplyRecord>
        implements TopApplyRecordService {

    private final ItemInfoService itemInfoService;

    @Override
    public void applyTop(ItemTopApplyDTO dto) {
        //将置顶申请存入数据库
        Long currentUserId = UserContext.getUserId();

        // 拦截 1：校验物品是否存在，且必须是本人发布的帖子才能申请
        ItemInfo itemInfo = itemInfoService.getById(dto.getItemId());
        if (itemInfo == null) {
            throw new BusinessException("申请置顶的物品不存在");
        }
        if (!itemInfo.getUserId().equals(currentUserId)) {
            throw new BusinessException(ResultCodeEnum.FORBIDDEN, "你只能申请置顶自己发布的帖子！");
        }

        // 拦截 2：校验该帖子是否已经在置顶状态了，防止重复套娃申请
        if (itemInfo.getIsTop().equals(TopEnum.YES.getCode())) {
            throw new BusinessException("该帖子已经在置顶中了，请勿重复申请！");
        }

        // 拦截 3：检查是否已经有一条“待审核”的记录
        boolean hasPendingApply = this.exists(
                new LambdaQueryWrapper<TopApplyRecord>()
                        .eq(TopApplyRecord::getItemId, dto.getItemId())
                        .eq(TopApplyRecord::getStatus, 0) // 0: 待审核
        );
        if (hasPendingApply) {
            throw new BusinessException("您已经提交过申请，正在等待管理员审核，请勿重复提交！");
        }

        // 全部校验通过，生成申请记录并入库
        TopApplyRecord record = new TopApplyRecord();
        record.setItemId(dto.getItemId());
        record.setUserId(currentUserId);
        record.setApplyReason(dto.getApplyReason());
        record.setStatus(0); // 设置为默认待审核状态

        this.save(record);
        log.info("申请置顶成功，用户ID: {} 提交了物品ID: {} 的置顶申请", currentUserId, dto.getItemId());

    }

    @Override
    public IPage<TopApplyRecord> pageQuery(AdminTopPageDTO dto) {
        // 1. 创建分页对象 (当前页, 每页大小)
        Page<TopApplyRecord> page = new Page<>(dto.getPage(), dto.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<TopApplyRecord> wrapper = new LambdaQueryWrapper<>();

        // 动态条件：如果前端传了状态 (0:待审核, 1:已通过, 2:已拒绝)，则加上该条件过滤
        wrapper.eq(dto.getStatus() != null, TopApplyRecord::getStatus, dto.getStatus());

        // 排序规则：按申请时间倒序排列（最新的申请排在最前面）
        wrapper.orderByDesc(TopApplyRecord::getCreateTime);

        // 3. 执行查询并返回结果
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveTopApply(AdminTopResolveDTO dto) {
        // 1. 获取这条申请记录
        TopApplyRecord record = this.getById(dto.getApplyId());
        if (record == null) {
            throw new BusinessException("该置顶申请记录不存在");
        }

        // 2. 防重复处理：只有“待审核(0)”的单子才能处理
        if (!record.getStatus().equals(TopApplyStatusEnum.PENDING.getCode())) {
            throw new BusinessException("该申请已被处理，请勿重复操作");
        }

        // 3. 根据动作执行
        if (dto.getAction().equals(AdminAuditActionEnum.PASS.getCode())) {
            // ======== 动作 1：同意置顶 ========
            record.setStatus(TopApplyStatusEnum.APPROVED.getCode());

            // 联动修改物品主表的置顶状态
            ItemInfo item = itemInfoService.getById(record.getItemId());
            if (item != null) {
                item.setIsTop(TopEnum.YES.getCode());
                // 设定过期时间：这里默认给它置顶 24小时
                item.setTopEndTime(LocalDateTime.now().plusHours(Constant.TOP_END_TIME_HOURS));

                itemInfoService.updateById(item);
                log.info("置顶联动成功：物品ID {} 已置顶，将在 {} 自动过期", item.getId(), item.getTopEndTime());
            }

        } else if (dto.getAction().equals(AdminAuditActionEnum.REJECT.getCode())) {
            // ======== 动作 2：拒绝置顶 ========
            record.setStatus(TopApplyStatusEnum.REJECTED.getCode());
            // 拒绝不需要联动主表
        }else {
            throw new BusinessException("无效的操作动作");
        }

        // 4. 保存管理员的审批备注
        if (StrUtil.isNotBlank(dto.getRemark())) {
            record.setProcessRemark(dto.getRemark());
        }

        // 5. 更新申请表状态
        this.updateById(record);
        log.info("管理员处理置顶申请成功，申请ID: {}, 处理动作: {}", dto.getApplyId(), dto.getAction());
    }
}




