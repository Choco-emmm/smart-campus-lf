package com.choco.smartlf.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.choco.smartlf.entity.dto.MessageSendDTO;
import com.choco.smartlf.entity.pojo.PrivateMessage;
import com.choco.smartlf.entity.pojo.User;
import com.choco.smartlf.entity.vo.ChatSessionVO;
import com.choco.smartlf.entity.vo.UserInfoVO;
import com.choco.smartlf.enums.ReadStatusEnum;
import com.choco.smartlf.mapper.PrivateMessageMapper;
import com.choco.smartlf.service.PrivateMessageService;
import com.choco.smartlf.service.UserService;
import com.choco.smartlf.utils.UserContext;
import com.choco.smartlf.utils.WsNoticeConstant;
import com.choco.smartlf.websocket.ChatWebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
* @author renpe
* @description 针对表【private_message(私信表)】的数据库操作Service实现
* @createDate 2026-04-10 12:55:01
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateMessageServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage>
    implements PrivateMessageService {

    private final UserService userService;
    // HTTP 接口调用
    @Override
    public PrivateMessage sendMessage(MessageSendDTO dto, Long senderId, boolean isReceiverOnline,boolean isReceiverFocusingMe) {
        if (senderId == null) {
            throw new RuntimeException("发送人ID不能为空");
        }

        UserInfoVO receiverInfo = userService.getUserInfo(dto.getReceiverId());

        if(receiverInfo == null){
            throw new RuntimeException("接收人不存在");
        }

        PrivateMessage privateMessage = new PrivateMessage();
        BeanUtil.copyProperties(dto, privateMessage);
        privateMessage.setSenderId(senderId);

        // 在线直接标记已读，离线标记未读
        if (isReceiverOnline) {
            privateMessage.setIsRead(ReadStatusEnum.READ.getCode());
        } else {
            privateMessage.setIsRead(ReadStatusEnum.UNREAD.getCode());
        }

        UserInfoVO senderInfo = userService.getUserInfo(senderId);

        save(privateMessage);
        log.info("发送私信成功，落库状态(1已读/0未读): {}", privateMessage.getIsRead());

        if(!isReceiverFocusingMe){
            //对方不在跟我聊天就发个通知给他
            ChatWebSocketServer.pushSystemNotice(dto.getReceiverId(),String.format(WsNoticeConstant.NEW_PRIVATE_MESSAGE,senderInfo.getNickname()));
        }

        return privateMessage;
    }



    @Override
    public List<PrivateMessage> getChatHistory(Long targetUserId) {
        Long myId = UserContext.getUserId();
        if(targetUserId.equals(myId)){
            throw new RuntimeException("不能给自己发私信");
        }

        // 1. 查询聊天历史记录，注意 OR/AND 的组合条件
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

        // 2. 点开会话后，将该会话内未读消息批量标记为已读
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

    @Override
    public List<ChatSessionVO> getSessionList() {
        Long myId = UserContext.getUserId();
        // 1. 获取所有和我有关的私信（按照创建时间倒序获取）
        List<PrivateMessage> myMessages = this.list(new LambdaQueryWrapper<PrivateMessage>()
                .eq(PrivateMessage::getSenderId, myId)
                .or()
                .eq(PrivateMessage::getReceiverId, myId)
                .orderByDesc(PrivateMessage::getCreateTime));

        if (CollUtil.isEmpty(myMessages)) {
            return new ArrayList<>();
        }

        // 2. 按会话对方 ID 分组
        // 逻辑：如果发件人是我，那对方就是收件人；如果发件人不是我，那对方就是发件人。
        Map<Long, List<PrivateMessage>> sessionMap = myMessages.stream()
                .collect(Collectors.groupingBy(
                        msg -> msg.getSenderId().equals(myId) ? msg.getReceiverId() : msg.getSenderId()
                ));

        // 3. 提取所有的对方用户 ID (拿到所有的 Key)
        Set<Long> targetUserIds = sessionMap.keySet();

        // 4. 批量查询用户信息并转为 Map
        Map<Long, User> userMap = userService.listByIds(targetUserIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 5. 组装会话 VO：消息、未读数、用户信息
        List<ChatSessionVO> resultList = sessionMap.entrySet().stream().map(entry -> {
                    Long targetId = entry.getKey();
                    List<PrivateMessage> chatHistory = entry.getValue(); // 这是我们俩所有的聊天记录（时间已倒序）

                    ChatSessionVO vo = new ChatSessionVO();
                    vo.setTargetUserId(targetId);

                    // ① 取出最新的一条消息的发送时间（因为原本查询就是倒序，所以 List 的第 0 个绝对是最新的！）
                    PrivateMessage lastMsg = chatHistory.getFirst();
                    vo.setLastMessageTime(lastMsg.getCreateTime());

                    // ② 统计未读数：在我们俩的记录里，接收人是我，且状态是未读的有多少条？
                    long unreadCount = chatHistory.stream()
                            .filter(m -> m.getReceiverId().equals(myId) && m.getIsRead().equals(ReadStatusEnum.UNREAD.getCode()))
                            .count();
                    vo.setUnreadCount((int) unreadCount);

                    // ③ 塞入对方的用户信息
                    User targetUser = userMap.get(targetId);
                    if (targetUser != null) {
                        vo.setTargetNickname(targetUser.getNickname());
                        vo.setTargetAvatar(targetUser.getAvatarUrl());
                    } else {
                        vo.setTargetNickname("已注销用户");
                        //todo 默认null了，前端自己弄头像吧
                    }

                    return vo;
                })
                // 6. 按最新消息时间倒序排列
                .sorted(Comparator.comparing(ChatSessionVO::getLastMessageTime).reversed())
                .toList();

        return resultList;

    }

    @Override
    public Integer getPrivateMessageNotifications() {
         Long unreadCount = count(new LambdaQueryWrapper<PrivateMessage>()
                .eq(PrivateMessage::getReceiverId, UserContext.getUserId())
                .eq(PrivateMessage::getIsRead, ReadStatusEnum.UNREAD.getCode()));
        return unreadCount.intValue();

    }
}




