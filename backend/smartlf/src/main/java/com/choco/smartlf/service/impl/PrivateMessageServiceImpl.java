package com.choco.smartlf.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.choco.smartlf.entity.dto.MessageSendDTO;
import com.choco.smartlf.entity.pojo.PrivateMessage;
import com.choco.smartlf.entity.vo.UserInfoVO;
import com.choco.smartlf.enums.ReadStatusEnum;
import com.choco.smartlf.service.PrivateMessageService;
import com.choco.smartlf.mapper.PrivateMessageMapper;
import com.choco.smartlf.service.UserService;
import com.choco.smartlf.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public List<PrivateMessage> getChatHistory(Long targetUserId) {
        Long myId = UserContext.getUserId();
        if(targetUserId.equals(myId)){
            throw new RuntimeException("不能给自己发私信");
        }

        // 🌟 1. 查询聊天历史记录 (注意 SQL 的 OR 和 AND 的嵌套逻辑)
        // 我们要查的是：(发送人是我 AND 接收人是他) 或者 (发送人是他 AND 接收人是我)
        List<PrivateMessage> history = this.list(new LambdaQueryWrapper<PrivateMessage>()
                // 第一组条件：我发给对方的
                .and(w -> w.eq(PrivateMessage::getSenderId, myId)
                        .eq(PrivateMessage::getReceiverId, targetUserId))
                // 第二组条件：对方发给我的
                .or(w -> w.eq(PrivateMessage::getSenderId, targetUserId)
                        .eq(PrivateMessage::getReceiverId, myId))
                // 必须按时间升序排列！这样在前端渲染时，老消息在上面，新消息在最底部的输入框上方
                .orderByAsc(PrivateMessage::getCreateTime)
        );

        // 🌟 2. 状态联动：精准消除未读红点
        // 既然我点开了这个聊天框，那么对方发给我的、且我还没读的消息，统统设为已读！
        this.update(new LambdaUpdateWrapper<PrivateMessage>()
                .set(PrivateMessage::getIsRead, ReadStatusEnum.READ.getCode())        // 改为已读
                .eq(PrivateMessage::getSenderId, targetUserId)                        // 条件1：发送人必须是对方
                .eq(PrivateMessage::getReceiverId, myId)                              // 条件2：接收人必须是我
                .eq(PrivateMessage::getIsRead, ReadStatusEnum.UNREAD.getCode())       // 条件3：只更新未读的，提高数据库性能
        );

        log.info("拉取聊天记录成功，当前用户ID: {}, 目标用户ID: {}, 共拉取 {} 条记录并自动消除未读状态",
                myId, targetUserId, history.size());

        return history;
    }
}




