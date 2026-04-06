package com.choco.smartlf.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.choco.smartlf.entity.pojo.UserActiveLog;
import com.choco.smartlf.mapper.UserActiveLogMapper;
import com.choco.smartlf.service.UserActiveLogService;
import org.springframework.stereotype.Service;

/**
* @author renpe
* @description 针对表【user_active_log(用户活跃流水表)】的数据库操作Service实现
* @createDate 2026-04-06 14:52:42
*/
@Service
public class UserActiveLogServiceImpl extends ServiceImpl<UserActiveLogMapper, UserActiveLog>
    implements UserActiveLogService {

}




