package com.choco.smartlf.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.choco.smartlf.entity.pojo.UserActiveLog;
import com.choco.smartlf.enums.ResultCodeEnum;
import com.choco.smartlf.exception.BusinessException;
import com.choco.smartlf.utils.Constant;
import com.choco.smartlf.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenAuthService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 核心鉴权方法：解析 JWT -> 查 Redis -> 拦截封禁 -> 续期 -> 记录活跃时间
     * @param token 客户端传来的 token
     * @return 成功则返回 Claims，失败则直接抛出对应的 BusinessException
     */
    public Claims authenticateAndRenew(String token) {
        if (StrUtil.isBlank(token)) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, "Token 为空");
        }

        // 1. 解析令牌
        Claims claims;
        try {
            claims = JwtUtil.parseJwt(token);
        } catch (Exception e) {
            log.error("非法令牌解析失败：{}", e.getMessage());
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, "无效的 Token");
        }

        Long userId = Long.valueOf(claims.get("userId").toString());
        Integer role = (Integer) claims.get("role");

        // 2. 校验 Redis
        String key = Constant.TOKEN_PREFIX + userId;
        String tokenInRedis = stringRedisTemplate.opsForValue().get(key);

        if (StrUtil.isBlank(tokenInRedis)) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, "登录已过期，请重新登录");
        }

        // 3. 判断是否被封禁
        if (Constant.TOKEN_BANNED_VALUE.equals(tokenInRedis)) {
            log.warn("拦截到已被强制封禁的用户请求，用户ID: {}", userId);
            throw new BusinessException(ResultCodeEnum.USER_BANNED);
        }

        // 4. 刷新 Token 在 Redis 里的续命时间
        stringRedisTemplate.expire(key, Constant.TOKEN_EXPIRATION, TimeUnit.MINUTES);

        // 5. 记录活跃时间
        UserActiveLog userActiveLog = new UserActiveLog();
        userActiveLog.setUserId(userId);
        userActiveLog.setRole(role);
        userActiveLog.setActiveTime(LocalDateTime.now());
        stringRedisTemplate.opsForHash().put(
                Constant.ACTIVE_TIME_KEY,
                userId.toString(),
                JSONUtil.toJsonStr(userActiveLog)
        );

        log.info("用户 {} 鉴权成功，角色：{}", userId, role);
        return claims; // 返回解析好的数据供后续使用
    }
}