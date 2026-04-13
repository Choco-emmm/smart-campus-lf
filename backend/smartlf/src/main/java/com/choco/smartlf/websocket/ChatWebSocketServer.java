package com.choco.smartlf.websocket;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.choco.smartlf.entity.dto.MessageSendDTO;
import com.choco.smartlf.entity.pojo.PrivateMessage;
import com.choco.smartlf.enums.WebSocketMsgTypeEnum;
import com.choco.smartlf.service.PrivateMessageService;
import com.choco.smartlf.service.impl.TokenAuthService;
import com.choco.smartlf.utils.Constant;
import io.jsonwebtoken.Claims;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint("/ws/chat/{token}")
@Component
public class ChatWebSocketServer {

    private static final ConcurrentHashMap<Long, Session> ONLINE_USERS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, Long> ACTIVE_WINDOWS = new ConcurrentHashMap<>();

    private static TokenAuthService tokenAuthService;
    private static PrivateMessageService privateMessageService;

    @Autowired
    public void setTokenAuthService(TokenAuthService tokenAuthService) {
        ChatWebSocketServer.tokenAuthService = tokenAuthService;
    }

    @Autowired
    public void setPrivateMessageService(PrivateMessageService privateMessageService) {
        ChatWebSocketServer.privateMessageService = privateMessageService;
    }

    private Long currentUserId;

    public static boolean isOnline(Long userId) {
        return ONLINE_USERS.containsKey(userId);
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        try {
            Claims claims = tokenAuthService.authenticateAndRenew(token);
            this.currentUserId = Long.valueOf(claims.get("userId").toString());
            ONLINE_USERS.put(this.currentUserId, session);
            log.info("【全局 WebSocket】用户 {} 上线", this.currentUserId);
        } catch (Exception e) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, e.getMessage()));
            } catch (Exception ignored) {}
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JSONObject json = JSONUtil.parseObj(message);
            String type = json.getStr(Constant.MSG_TYPE_KEY);

            // 1. 焦点上报
            if (WebSocketMsgTypeEnum.ACTIVE_WINDOW.getType().equals(type)) {
                Long targetId = json.getLong("targetId");
                if (targetId == null) {
                    ACTIVE_WINDOWS.remove(this.currentUserId);
                } else {
                    ACTIVE_WINDOWS.put(this.currentUserId, targetId);
                }
                return;
            }

            // 2. 聊天消息
            if (WebSocketMsgTypeEnum.CHAT.getType().equals(type)) {
                MessageSendDTO messageDTO = JSONUtil.toBean(json, MessageSendDTO.class);
                if (messageDTO.getContent() == null || messageDTO.getContent().trim().isEmpty()) {
                    return;
                }

                Long receiverFocusId = ACTIVE_WINDOWS.get(messageDTO.getReceiverId());
                boolean isReceiverFocusingMe = (receiverFocusId != null && receiverFocusId.equals(this.currentUserId));

                PrivateMessage savedMessage = privateMessageService.sendMessage(messageDTO, this.currentUserId, isReceiverFocusingMe);

                if (isOnline(messageDTO.getReceiverId())) {
                    // 🌟 核心修改：包装成带 type 的标准协议发送给前端
                    JSONObject pushData = new JSONObject();
                    pushData.set(Constant.MSG_TYPE_KEY, WebSocketMsgTypeEnum.CHAT);
                    pushData.set(Constant.MSG_DATA_KEY, savedMessage);
                    pushMessage(messageDTO.getReceiverId(), pushData);
                }
            }
        } catch (Exception e) {
            log.error("【全局 WebSocket】处理消息失败", e);
        }
    }

    @OnClose
    public void onClose() {
        if (this.currentUserId != null) {
            ONLINE_USERS.remove(this.currentUserId);
            ACTIVE_WINDOWS.remove(this.currentUserId);
            log.info("【全局 WebSocket】用户 {} 下线", this.currentUserId);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("【全局 WebSocket】发生错误", error);
    }

    private static void pushMessage(Long targetUserId, Object messageObj) {
        Session session = ONLINE_USERS.get(targetUserId);
        if (session != null && session.isOpen()) {
            try {
                session.getAsyncRemote().sendText(JSONUtil.toJsonStr(messageObj));
            } catch (Exception ignored) {}
        }
    }

    // 🌟 新增：供 ItemCommentServiceImpl 调用的公共通知方法
    public static void pushNotice(Long targetUserId) {
        if (isOnline(targetUserId)) {
            JSONObject pushData = new JSONObject();
            pushData.set(Constant.MSG_TYPE_KEY, WebSocketMsgTypeEnum.NOTICE);
            pushMessage(targetUserId, pushData);
        }
    }

    public static void pushClaimNotice(Long targetUserId) {
        if (isOnline(targetUserId)) {
            JSONObject pushData = new JSONObject();
            pushData.set(Constant.MSG_TYPE_KEY, WebSocketMsgTypeEnum.CLAIM); // 🌟 专门用于认领申请状态变更的通知
            pushMessage(targetUserId, pushData);
        }
    }
}