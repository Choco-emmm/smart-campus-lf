package com.choco.smartlf.websocket;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.choco.smartlf.exception.BusinessException;
import com.choco.smartlf.service.impl.TokenAuthService;
import com.choco.smartlf.utils.Constant;
import com.choco.smartlf.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint("/ws/chat/{token}")
@Component
public class ChatWebSocketServer {

    // 记录在线的用户集
    private static final ConcurrentHashMap<Long, Session> ONLINE_USERS = new ConcurrentHashMap<>();
    
    // 🌟 静态注入 RedisTemplate
    private static StringRedisTemplate stringRedisTemplate;

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        ChatWebSocketServer.stringRedisTemplate = stringRedisTemplate;
    }
    private static TokenAuthService tokenAuthService;

    @Autowired
    public void setTokenAuthService(TokenAuthService tokenAuthService) {
        ChatWebSocketServer.tokenAuthService = tokenAuthService;
    }
    //当前实例的用户的id
    private Long currentUserId;

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        try {
            // 1. 一行代码完成：解析JWT、Redis校验、封禁拦截、续命、记录活跃时间！
            Claims claims = tokenAuthService.authenticateAndRenew(token);

            this.currentUserId = Long.valueOf(claims.get("userId").toString());
            ONLINE_USERS.put(this.currentUserId, session);

            log.info("【WebSocket】用户 {} 上线", this.currentUserId);

        } catch (BusinessException e) {
            // 捕获 BusinessException 抛出的各类详细错误 (封禁、过期等)
            log.warn("【WebSocket】连接被拒：{}", e.getMessage());
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, e.getMessage()));
            } catch (Exception ignored) {}
        } catch (Exception e) {
            log.error("【WebSocket】未知的连接错误");
        }
    }

    @OnClose
    public void onClose() {
        if (this.currentUserId != null) {
            ONLINE_USERS.remove(this.currentUserId);
            log.info("【WebSocket】用户 {} 下线，当前在线人数：{}", this.currentUserId, ONLINE_USERS.size());
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("【WebSocket】发生错误: {}", error.getMessage());
    }

    /**
     * 给 PrivateMessageServiceImpl 调用的推送方法
     */
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