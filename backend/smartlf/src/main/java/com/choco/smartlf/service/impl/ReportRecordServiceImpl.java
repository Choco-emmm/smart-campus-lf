package com.choco.smartlf.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.choco.smartlf.entity.dto.ItemReportDTO;
import com.choco.smartlf.entity.pojo.ItemInfo;
import com.choco.smartlf.entity.pojo.ReportRecord;
import com.choco.smartlf.enums.ReportRecordEnum;
import com.choco.smartlf.exception.BusinessException;
import com.choco.smartlf.mapper.ReportRecordMapper;
import com.choco.smartlf.service.ReportRecordService;
import com.choco.smartlf.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    private final ItemInfoServiceImpl itemInfoService;

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
}




