package com.choco.smartlf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.choco.smartlf.entity.dto.ItemTopApplyDTO;
import com.choco.smartlf.entity.pojo.ItemInfo;
import com.choco.smartlf.entity.pojo.TopApplyRecord;
import com.choco.smartlf.enums.ResultCodeEnum;
import com.choco.smartlf.enums.TopEnum;
import com.choco.smartlf.exception.BusinessException;
import com.choco.smartlf.service.ItemInfoService;
import com.choco.smartlf.service.TopApplyRecordService;
import com.choco.smartlf.mapper.TopApplyRecordMapper;
import com.choco.smartlf.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}




