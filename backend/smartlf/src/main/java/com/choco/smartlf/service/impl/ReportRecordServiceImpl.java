package com.choco.smartlf.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.choco.smartlf.entity.dto.AdminReportPageDTO;
import com.choco.smartlf.entity.dto.AdminReportResolveDTO;
import com.choco.smartlf.entity.dto.ItemReportDTO;
import com.choco.smartlf.entity.pojo.ItemInfo;
import com.choco.smartlf.entity.pojo.ReportRecord;
import com.choco.smartlf.entity.pojo.User;
import com.choco.smartlf.entity.vo.AdminReportDetailVO;
import com.choco.smartlf.enums.AdminAuditActionEnum;
import com.choco.smartlf.enums.ItemStatusEnum;
import com.choco.smartlf.enums.ReportRecordEnum;
import com.choco.smartlf.enums.TopEnum;
import com.choco.smartlf.exception.BusinessException;
import com.choco.smartlf.mapper.ReportRecordMapper;
import com.choco.smartlf.service.ItemInfoService;
import com.choco.smartlf.service.ReportRecordService;
import com.choco.smartlf.service.UserService;
import com.choco.smartlf.utils.UserContext;
import com.choco.smartlf.utils.WsNoticeConstant;
import com.choco.smartlf.websocket.ChatWebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author renpe
* @description 针对表【report_record(举报管理表)】的数据库操作Service实现
* @createDate 2026-04-06 14:52:42
*/
@Slf4j
@RequiredArgsConstructor
@Service
public class ReportRecordServiceImpl extends ServiceImpl<ReportRecordMapper, ReportRecord>
    implements ReportRecordService {

    private final ItemInfoService itemInfoService;
    private final UserService userService;

    @Override
    public void submitReport(ItemReportDTO dto) {
        //获取举报人ID
        Long reporterId = UserContext.getUserId();

        //校验物品是否存在
        ItemInfo item = itemInfoService.getById(dto.getItemId());
        if (item == null) {
            throw new BusinessException("被举报的物品不存在！");
        }
        //检查被举报的失物信息是不是自己的（前端会做检查，如果用户在看自己的帖子就不会出现举报按钮，但这里还是做个核验）
        if (item.getUserId().equals(reporterId)) {
            throw new BusinessException("你不能举报自己发布的帖子！");
        }
       // 校验是否重复举报 (相同用户+同一个失物信息下，状态为 0:待处理 的记录只能有一条)
        boolean exists = exists(new LambdaQueryWrapper<ReportRecord>()
                .eq(ReportRecord::getItemId, dto.getItemId())
                .eq(ReportRecord::getReporterId, reporterId)
                .eq(ReportRecord::getStatus, ReportRecordEnum.WAITING.getCode()));
        if (exists) {
            throw new BusinessException("您已提交过举报，请等待管理员处理");
        }
        //数据库那边会自动把创建时间和状态进行默认设置，就不手动了
        ReportRecord reportRecord = new ReportRecord();
        BeanUtil.copyProperties(dto, reportRecord);
        reportRecord.setReporterId(reporterId);
        save(reportRecord);
        log.info("用户{}提交了举报，举报物品ID为{}，举报理由为{}", reporterId, dto.getItemId(), dto.getReason());
    }

    @Override
    public IPage<ReportRecord> pageQuery(AdminReportPageDTO dto) {
        // 1. 创建分页对象 (当前页, 每页大小)
        IPage<ReportRecord> page = new Page<>(dto.getPage(), dto.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<ReportRecord> queryWrapper = new LambdaQueryWrapper<>();

        // 如果传了状态举报状态 (0:待处理, 1:已核实, 2:已驳回)，则进行过滤
        queryWrapper.eq(dto.getStatus() != null, ReportRecord::getStatus, dto.getStatus());
        // 如果传了举报理由
        queryWrapper.like(StrUtil.isNotBlank(dto.getReason()), ReportRecord::getReason, dto.getReason());
        // 排序，通常举报记录应该按“创建时间”倒序排列，让管理员先看到最新的举报
        queryWrapper.orderByDesc(ReportRecord::getCreateTime);

        // 3. 执行查询并返回
        return this.page(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务控制
    public void resolveReport(AdminReportResolveDTO dto) {
        // 1. 查询该举报记录
        ReportRecord report = this.getById(dto.getReportId());
        if (report == null) {
            throw new BusinessException("该举报记录不存在！");
        }

        // 2. 防止管理员重复处理同一条举报
        if (!report.getStatus().equals(ReportRecordEnum.WAITING.getCode())) {
            throw new BusinessException("该举报已被处理，请勿重复操作！");
        }

        // 3. 根据管理员的动作进行分支处理
        if (dto.getAction().equals(AdminAuditActionEnum.PASS.getCode())) {
            // ================= 动作 1：核实违规并下架 =================
            // a. 修改举报单状态为“已核实”
            report.setStatus(ReportRecordEnum.VERIFYING.getCode());

            // b. 联动修改对应的物品状态
            ItemInfo item = itemInfoService.getById(report.getItemId());
            if (item != null) {
                // 将帖子状态改为“违规下架(3)”
                item.setStatus(ItemStatusEnum.BANNED.getCode());

                // 细节：如果这个帖子是置顶帖子，违规了必须同时取消置顶
                if (item.getIsTop().equals(TopEnum.YES.getCode())) {
                    item.setIsTop(TopEnum.NO.getCode());
                    item.setTopEndTime(null);
                }

                itemInfoService.updateById(item);
                //通知原帖主
                User target = userService.getById(item.getUserId());
                if(target!=null){
                    ChatWebSocketServer.pushSystemNotice(target.getId(),String.format(WsNoticeConstant.ITEM_VERIFIED_DOWN, item.getPublicDesc()));
                }

                log.info("举报核实联动：物品ID {} 已被违规下架", item.getId());
            }

        } else if (dto.getAction().equals(AdminAuditActionEnum.REJECT.getCode())) {
            // ================= 动作 2：驳回举报 =================
            // 修改举报单状态为“已驳回”，不用动原帖子
            report.setStatus(ReportRecordEnum.REJECTED.getCode());
        } else {
            throw new BusinessException("无效的处理动作参数！");
        }

        // 4. 记录管理员的审批备注 (如果有的话)
        if (StrUtil.isNotBlank(dto.getRemark())) {
            report.setProcessRemark(dto.getRemark());
        }

        // 5. 保存举报单的修改
        this.updateById(report);

        log.info("管理员处理举报成功，举报单ID: {}, 处理动作: {}", dto.getReportId(), dto.getAction());
    }

    @Override
    public AdminReportDetailVO getReportDetail(Long reportId) {
        // 1. 查出举报单
        ReportRecord report = this.getById(reportId);
        if (report == null) {
            throw new BusinessException("该举报记录不存在");
        }

        AdminReportDetailVO vo = new AdminReportDetailVO();
        vo.setReportId(report.getId());
        vo.setReason(report.getReason());
        vo.setItemId(report.getItemId());
        // 直接从举报记录里拿到举报人 ID
        vo.setReporterId(report.getReporterId());

        // 2. 查举报人昵称
        User reporter = userService.getById(report.getReporterId());
        vo.setReporterNickname(reporter != null ? reporter.getNickname() : "已注销用户");

        // 3. 查原帖题目
        ItemInfo item = itemInfoService.getById(report.getItemId());
        vo.setItemTitle(item != null ? item.getPublicDesc() : "【原帖已被删除】");

        return vo;
    }
}




