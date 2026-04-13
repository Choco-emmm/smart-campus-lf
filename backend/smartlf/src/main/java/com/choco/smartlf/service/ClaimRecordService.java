package com.choco.smartlf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.choco.smartlf.entity.dto.ClaimAuditDTO;
import com.choco.smartlf.entity.dto.ClaimSubmitDTO;
import com.choco.smartlf.entity.dto.ClaimSupplementDTO;
import com.choco.smartlf.entity.pojo.ClaimRecord;

/**
* @author Choco
* @description 针对表【claim_record(认领申请记录表)】的数据库操作Service
* @createDate 2026-04-06 14:52:41
*/
public interface ClaimRecordService extends IService<ClaimRecord> {

    void submitClaim(ClaimSubmitDTO dto);

    void supplementClaim(ClaimSupplementDTO dto);

    void auditClaim(ClaimAuditDTO dto);
}
