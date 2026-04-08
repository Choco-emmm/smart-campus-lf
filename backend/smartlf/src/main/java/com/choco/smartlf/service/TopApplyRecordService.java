package com.choco.smartlf.service;

import com.choco.smartlf.entity.dto.ItemTopApplyDTO;
import com.choco.smartlf.entity.pojo.TopApplyRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author renpe
* @description 针对表【top_apply_record(置顶申请记录表)】的数据库操作Service
* @createDate 2026-04-08 09:43:35
*/
public interface TopApplyRecordService extends IService<TopApplyRecord> {

    void applyTop(ItemTopApplyDTO dto);
}
