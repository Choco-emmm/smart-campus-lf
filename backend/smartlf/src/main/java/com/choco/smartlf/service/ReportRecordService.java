package com.choco.smartlf.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.choco.smartlf.entity.dto.AdminReportPageDTO;
import com.choco.smartlf.entity.dto.AdminReportResolveDTO;
import com.choco.smartlf.entity.dto.ItemReportDTO;
import com.choco.smartlf.entity.pojo.ReportRecord;
import com.choco.smartlf.entity.vo.AdminReportDetailVO;

/**
* @author renpe
* @description 针对表【report_record(举报管理表)】的数据库操作Service
* @createDate 2026-04-06 14:52:42
*/
public interface ReportRecordService extends IService<ReportRecord> {

    void submitReport(ItemReportDTO dto);

    IPage<ReportRecord> pageQuery(AdminReportPageDTO dto);

    void resolveReport(AdminReportResolveDTO dto);

    AdminReportDetailVO getReportDetail(Long reportId);
}
