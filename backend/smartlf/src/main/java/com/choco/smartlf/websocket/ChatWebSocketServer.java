package com.choco.smartlf.websocket;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.choco.smartlf.entity.dto.MessageSendDTO;
import com.choco.smartlf.entity.pojo.PrivateMessage;
import com.choco.smartlf.enums.WebSocketMsgTypeEnum;
import com.choco.smartlf.service.PrivateMessageService;
import com.choco.smartlf.service.impl.TokenAuthService;
import com.choco.smartlf.utils.Constant;
import com.choco.smartlf.utils.WsNoticeConstant;
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
    /**
     * 'A的id,B的id'
     * 表示A正在查看与B的会话
     */
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
    private String token;

    public static boolean isOnline(Long userId) {
        return ONLINE_USERS.containsKey(userId);
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        try {
            Claims claims = tokenAuthService.authenticateAndRenew(token);
            this.currentUserId = Long.valueOf(claims.get("userId").toString());
            this.token=token;
            ONLINE_USERS.put(this.currentUserId, session);
            log.info("【全局 WebSocket】用户 {} 上线", this.currentUserId);
        } catch (Exception e) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, e.getMessage()));
            } catch (Exception ignored) {}
        }
    }

    @OnMessage
    public void onMessage(String message,Session session) {
        //验权
        try {
            tokenAuthService.authenticateAndRenew(token);
        } catch (Exception e) {
            log.warn("【全局 WebSocket】用户 {} 消息验权失败: {}", this.currentUserId, e.getMessage());

            // 🌟 2. 伪造一个系统级的 error 消息发给前端
            JSONObject errorJson = new JSONObject();
            errorJson.set(Constant.MSG_TYPE_KEY, "error");
            errorJson.set("code", 401);
            errorJson.set("message", "登录已失效，请重新登录");

            try {
               session.getBasicRemote().sendText(errorJson.toString());
                // 🌟 3. 发完通知后，服务端主动把这个非法的连接掐断
                session.close();
            } catch (Exception ioException) {
                log.error("断开失效 WebSocket 异常", ioException);
            }
            return; // 直接 return，不执行后续业务逻辑
        }
        try {
            JSONObject json = JSONUtil.parseObj(message);
            String type = json.getStr(Constant.MSG_TYPE_KEY);

            // 1. 焦点上报
            if (WebSocketMsgTypeEnum.ACTIVE_WINDOW.getType().equals(type)) {
                Long targetId = json.getLong("targetId");
                if (targetId == null) {
                    ACTIVE_WINDOWS.remove(this.currentUserId);
                    log.info("【全局 WebSocket】用户 {} 取消查看任何人", this.currentUserId);
                } else {
                    ACTIVE_WINDOWS.put(this.currentUserId, targetId);
                    log.info("【全局 WebSocket】用户 {} 查看了用户 {}", this.currentUserId, targetId);
                }
                return;
            }

            // 2. 聊天消息
            if (WebSocketMsgTypeEnum.CHAT.getType().equals(type)) {
                log.info("【全局 WebSocket】用户 {} 发送消息", this.currentUserId);
                MessageSendDTO messageDTO = JSONUtil.toBean(json, MessageSendDTO.class);
                if (messageDTO.getContent() == null || messageDTO.getContent().trim().isEmpty()) {
                    return;
                }

                Long receiverFocusId = ACTIVE_WINDOWS.get(messageDTO.getReceiverId());
                /**
                 * 对方正在聊天
                 */
                boolean isReceiverChatting= receiverFocusId != null;
                /**
                 * 对方正在查看跟我的聊天窗口
                 */
                boolean isReceiverFocusingMe = (receiverFocusId != null && receiverFocusId.equals(this.currentUserId));

                //将我发的消息先存入库
                //如果对方没focus我，默认未读。若对方focus我，我发给对方消息都设为已读
                //对方没在聊天，全局通知一下
                PrivateMessage savedMessage = privateMessageService.sendMessage(messageDTO, this.currentUserId, isReceiverFocusingMe,isReceiverChatting);

                //对方在线，传输给对方
                if (isOnline(messageDTO.getReceiverId())) {
                    // 🌟 核心修改：包装成带 type 的标准协议发送给前端
                    JSONObject pushData = new JSONObject();
                    pushData.set(Constant.MSG_TYPE_KEY, WebSocketMsgTypeEnum.CHAT.getType());
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

    /**
     * 推送聊天消息
     * @param targetUserId 目标用户 ID
     * @param messageObj 带 type 的标准协议的消息对象
     */
    private static void pushMessage(Long targetUserId, Object messageObj) {
        Session session = ONLINE_USERS.get(targetUserId);
        //对方在线
        if (session != null && session.isOpen()) {
            try {
                session.getAsyncRemote().sendText(JSONUtil.toJsonStr(messageObj));
            } catch (Exception ignored) {}
        }
    }

//    /**
//     * 发留言之后小窗通知帖主
//     * @param targetUserId 目标用户 ID
//     */
//    public static void pushNotice(Long targetUserId) {
//        //帖主在线，发通知给他
//        if (isOnline(targetUserId)) {
//            JSONObject pushData = new JSONObject();
//            pushData.set(Constant.MSG_TYPE_KEY, WebSocketMsgTypeEnum.NOTICE);
//            pushMessage(targetUserId, pushData);
//        }
//    }

//    /**
//     * 认领申请通知
//     * @param targetUserId 帖主 ID
//     */
//    public static void pushClaimNotice(Long targetUserId) {
//        if (isOnline(targetUserId)) {
//            JSONObject pushData = new JSONObject();
//            pushData.set(Constant.MSG_TYPE_KEY, WebSocketMsgTypeEnum.NOTICE); // 🌟 专门用于认领申请状态变更的通知
//            pushMessage(targetUserId, pushData);
//        }
//    }
    /**
     * 系统通知，传入要通知的人和内容
     */
    public static void pushSystemNotice(Long targetUserId, String content) {
        if (isOnline(targetUserId)) {
            JSONObject pushData = new JSONObject();
            pushData.set(Constant.MSG_TYPE_KEY, WebSocketMsgTypeEnum.NOTICE.getType());
            pushData.set(Constant.MSG_CONTENT_KEY, content);
            pushMessage(targetUserId, pushData);
        }
    }
}