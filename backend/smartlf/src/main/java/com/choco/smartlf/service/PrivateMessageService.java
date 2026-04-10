package com.choco.smartlf.service;

import com.choco.smartlf.entity.dto.MessageSendDTO;
import com.choco.smartlf.entity.pojo.PrivateMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.choco.smartlf.entity.vo.ChatSessionVO;

import java.util.List;

/**
* @author renpe
* @description 针对表【private_message(私信表)】的数据库操作Service
* @createDate 2026-04-10 12:55:01
*/
public interface PrivateMessageService extends IService<PrivateMessage> {

    void sendMessage(MessageSendDTO dto);

    List<PrivateMessage> getChatHistory(Long targetUserId);

    List<ChatSessionVO> getSessionList();

    Integer getPrivateMessageNotifications();
}
