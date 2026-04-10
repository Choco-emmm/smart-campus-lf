package com.choco.smartlf.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.choco.smartlf.entity.dto.MessageSendDTO;
import com.choco.smartlf.entity.pojo.PrivateMessage;
import com.choco.smartlf.entity.vo.UserInfoVO;
import com.choco.smartlf.service.PrivateMessageService;
import com.choco.smartlf.mapper.PrivateMessageMapper;
import com.choco.smartlf.service.UserService;
import com.choco.smartlf.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
* @author renpe
* @description 针对表【private_message(私信表)】的数据库操作Service实现
* @createDate 2026-04-10 12:55:01
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateMessageServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage>
    implements PrivateMessageService{

    private final UserService userService;


    @Override
    public void sendMessage(MessageSendDTO dto) {
        //先确认一下能不能找到传入的id的用户
        UserInfoVO userInfo = userService.getUserInfo(dto.getReceiverId());
        if(userInfo == null){
            throw new RuntimeException("接收人不存在");
        }
        //获取当前用户的id
        Long senderId = UserContext.getUserId();
        PrivateMessage privateMessage = new PrivateMessage();
        BeanUtil.copyProperties(dto, privateMessage);
        privateMessage.setSenderId(senderId);
        save(privateMessage);
        log.info("发送私信成功: {}", privateMessage);
    }
}




