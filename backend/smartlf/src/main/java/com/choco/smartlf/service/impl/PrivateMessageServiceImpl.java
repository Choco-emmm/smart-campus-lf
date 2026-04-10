package com.choco.smartlf.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.choco.smartlf.entity.pojo.PrivateMessage;
import com.choco.smartlf.service.PrivateMessageService;
import com.choco.smartlf.mapper.PrivateMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author renpe
* @description 针对表【private_message(私信表)】的数据库操作Service实现
* @createDate 2026-04-10 12:55:01
*/
@Service
public class PrivateMessageServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage>
    implements PrivateMessageService{

}




