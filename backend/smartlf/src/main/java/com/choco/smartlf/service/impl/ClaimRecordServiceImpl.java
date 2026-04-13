package com.choco.smartlf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.choco.smartlf.entity.dto.ClaimSubmitDTO;
import com.choco.smartlf.entity.dto.ClaimSupplementDTO;
import com.choco.smartlf.entity.pojo.ClaimRecord;
import com.choco.smartlf.entity.pojo.ItemInfo;
import com.choco.smartlf.entity.pojo.ItemSecure;
import com.choco.smartlf.enums.ClaimStatusEnum;
import com.choco.smartlf.enums.ItemStatusEnum;
import com.choco.smartlf.exception.BusinessException;
import com.choco.smartlf.mapper.ClaimRecordMapper;
import com.choco.smartlf.service.ClaimRecordService;
import com.choco.smartlf.service.ItemInfoService;
import com.choco.smartlf.service.ItemSecureService;
import com.choco.smartlf.utils.UserContext;
import com.choco.smartlf.websocket.ChatWebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author renpe
 * @description 针对表【claim_record(认领申请记录表)】的数据库操作Service实现
 * @createDate 2026-04-06 14:52:41
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ClaimRecordServiceImpl extends ServiceImpl<ClaimRecordMapper, ClaimRecord>
        implements ClaimRecordService {

    private final ItemInfoService itemInfoService;
    private final ItemSecureService itemSecureService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitClaim(ClaimSubmitDTO dto) {
        // 1. 获取当前操作的用户 ID (认领者)
        Long currentUserId = UserContext.getUserId();

        // 2. 查询帖子信息
        ItemInfo iteminfo = itemInfoService.getById(dto.getItemId());
        if (iteminfo == null) {
            throw new BusinessException("该物品不存在或已被删除");
        }

        // 3. 校验：不能认领自己发布的物品
        if (iteminfo.getUserId().equals(currentUserId)) {
            throw new BusinessException("不能认领自己发布的物品！");
        }

        //校验：不能申请锁定/结案/违规下架的帖子（只能申请寻找中的帖子）
        Integer status = iteminfo.getStatus();
        if(!ItemStatusEnum.SEARCHING.getCode().equals(status)){
            throw new BusinessException("不能申请锁定/结案/违规下架的帖子");
        }

        // 4.校验：不能申请没核验字段的帖子
        ItemSecure itemSecure = itemSecureService.getById(iteminfo.getId());
        if (itemSecure == null){
            throw new BusinessException("物品未设置核验问题，请勿申请");
        }



        // 4. 防刷校验：检查该用户是否已经对这个物品发起过申请（且状态不是“已拒绝”）
        // 也就是说，如果正在审核中（0），或者要求补充（3），或者已经补充（4），都不允许再发起新的申请
        long existCount = this.count(new LambdaQueryWrapper<ClaimRecord>()
                .eq(ClaimRecord::getItemId, dto.getItemId())
                .eq(ClaimRecord::getApplicantId, currentUserId)
                .in(ClaimRecord::getStatus,
                        ClaimStatusEnum.PENDING.getCode(),
                        ClaimStatusEnum.REQUIRE_SUPPLEMENT.getCode(),
                        ClaimStatusEnum.SUPPLEMENT_SUBMITTED.getCode())
        );
        if (existCount > 0) {
            throw new BusinessException("您已有处理中的申请，请勿重复提交");
        }

        // 5. 拼装认领记录实体
        ClaimRecord record = new ClaimRecord();
        record.setItemId(dto.getItemId());
        record.setApplicantId(currentUserId);     // 申请人是当前登录用户
        record.setPublisherId(iteminfo.getUserId());  // 帖子的主人是谁
        record.setAnswer(dto.getClaimAnswer());

        // 🌟 优雅使用枚举类：状态设置为 0 (待审核)
        record.setStatus(ClaimStatusEnum.PENDING.getCode());

        // 保存入库
        this.save(record);
        log.info("用户 {} 提交认领申请成功，申请单ID：{}", currentUserId, record.getId());

        // 6. 神级闭环：通知发帖人“有人来认领了”
        // 这里的 item.getUserId() 就是发帖人的 ID
        ChatWebSocketServer.pushClaimNotice(iteminfo.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void supplementClaim(ClaimSupplementDTO dto) {
        // 1. 获取当前登录用户 ID (认领者)
        Long currentUserId = UserContext.getUserId();

        // 2. 查询该条认领记录
        ClaimRecord record = this.getById(dto.getClaimId());
        if (record == null) {
            throw new BusinessException("认领申请不存在");
        }
        //查询原帖
        ItemInfo iteminfo = itemInfoService.getById(record.getItemId());
        if (iteminfo == null){
            throw new BusinessException("失物信息不存在");
        }
        //校验：不能申请锁定/结案/违规下架的帖子（只能申请寻找中的帖子）
        Integer status = iteminfo.getStatus();
        if(!ItemStatusEnum.SEARCHING.getCode().equals(status)){
            throw new BusinessException("不能申请锁定/结案/违规下架的帖子");
        }

        // 3. 越权校验：必须是原本提交申请的那个人，才能补充
        if (!record.getApplicantId().equals(currentUserId)) {
            throw new BusinessException("非法操作：您不是该申请的起草人");
        }

        // 4. 状态校验：只有在“要求补充(状态3)”的情况下，才能提交补充
        if (!record.getStatus().equals(ClaimStatusEnum.REQUIRE_SUPPLEMENT.getCode())) {
            throw new BusinessException("当前状态不需要补充证据");
        }

        // 5. 更新补充内容并流转状态
        record.setSupplementAnswer(dto.getSupplementAnswer());
        // 🌟 状态变更为 4：已补充细节，等待发布者二次审核
        record.setStatus(ClaimStatusEnum.SUPPLEMENT_SUBMITTED.getCode());

        this.updateById(record);

        // 6. 实时通知发布者（即帖主）
        // 告诉帖主：那个物品 ID 为 record.getItemId() 的申请单已经更新了
        com.choco.smartlf.websocket.ChatWebSocketServer.pushClaimNotice(record.getPublisherId());
    }
}




