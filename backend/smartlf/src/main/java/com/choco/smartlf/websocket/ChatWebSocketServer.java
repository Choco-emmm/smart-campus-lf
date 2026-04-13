package com.choco.smartlf.websocket;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.choco.smartlf.entity.dto.MessageSendDTO;
import com.choco.smartlf.entity.pojo.PrivateMessage;
import com.choco.smartlf.service.PrivateMessageService;
import com.choco.smartlf.service.impl.TokenAuthService;
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

    // 记录在线的 WebSocket Session
    private static final ConcurrentHashMap<Long, Session> ONLINE_USERS = new ConcurrentHashMap<>();

    // 🌟 新增：记录谁正在看谁的窗口。Key: 自己的ID, Value: 正在对话的目标用户ID
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

    // 判断某人是否在线 (网络连接中)
    public static boolean isOnline(Long userId) {
        return ONLINE_USERS.containsKey(userId);
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        try {
            Claims claims = tokenAuthService.authenticateAndRenew(token);
            this.currentUserId = Long.valueOf(claims.get("userId").toString());
            ONLINE_USERS.put(this.currentUserId, session);
            log.info("【WebSocket】用户 {} 上线", this.currentUserId);
        } catch (Exception e) {
            log.warn("【WebSocket】连接被拒：{}", e.getMessage());
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, e.getMessage()));
            } catch (Exception ignored) {}
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            // 解析前端发来的 JSON，引入 type 字段进行路由
            JSONObject json = JSONUtil.parseObj(message);
            String type = json.getStr("type");

            // 🌟 场景 1：前端汇报窗口焦点切换
            if ("active_window".equals(type)) {
                Long targetId = json.getLong("targetId");
                if (targetId == null) {
                    ACTIVE_WINDOWS.remove(this.currentUserId); // 离开了聊天窗
                } else {
                    ACTIVE_WINDOWS.put(this.currentUserId, targetId); // 切到了某人的聊天窗
                }
                return;
            }

            // 🌟 场景 2：前端发送真实聊天消息
            if ("chat".equals(type)) {
                MessageSendDTO messageDTO = JSONUtil.toBean(json, MessageSendDTO.class);
                if (messageDTO.getContent() == null || messageDTO.getContent().trim().isEmpty()) {
                    return;
                }

                // 核心判断：对方此时此刻的焦点是不是我？
                Long receiverFocusId = ACTIVE_WINDOWS.get(messageDTO.getReceiverId());
                boolean isReceiverFocusingMe = (receiverFocusId != null && receiverFocusId.equals(this.currentUserId));

                // 存入数据库，isReceiverFocusingMe 为 true 则直接标为已读
                PrivateMessage savedMessage = privateMessageService.sendMessage(messageDTO, this.currentUserId, isReceiverFocusingMe);

                // 如果对方连着网 (哪怕没在看我的窗口)，也把消息推给他，让他前端更新红点
                if (isOnline(messageDTO.getReceiverId())) {
                    pushMessage(messageDTO.getReceiverId(), savedMessage);
                }
            }
        } catch (Exception e) {
            log.error("【WebSocket】处理客户端消息失败", e);
        }
    }

    @OnClose
    public void onClose() {
        if (this.currentUserId != null) {
            ONLINE_USERS.remove(this.currentUserId);
            ACTIVE_WINDOWS.remove(this.currentUserId); // 下线时同步清理焦点状态
            log.info("【WebSocket】用户 {} 下线", this.currentUserId);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("【WebSocket】发生错误: {}", error.getMessage());
    }

    public static void pushMessage(Long targetUserId, Object messageObj) {
        Session session = ONLINE_USERS.get(targetUserId);
        if (session != null && session.isOpen()) {
            try {
                session.getAsyncRemote().sendText(JSONUtil.toJsonStr(messageObj));
            } catch (Exception e) {
                log.error("【WebSocket】消息推送失败", e);
            }
        }
    }
}