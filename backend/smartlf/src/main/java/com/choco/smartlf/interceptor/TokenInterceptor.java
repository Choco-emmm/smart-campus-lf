package com.choco.smartlf.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.choco.smartlf.entity.pojo.UserActiveLog;
import com.choco.smartlf.enums.ResultCodeEnum;
import com.choco.smartlf.enums.RoleEnum;
import com.choco.smartlf.exception.BusinessException;
import com.choco.smartlf.utils.Constant;
import com.choco.smartlf.utils.JwtUtil;
import com.choco.smartlf.utils.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.choco.smartlf.utils.Constant.ACTIVE_TIME_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 放行浏览器的预检请求 (OPTIONS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 2. 获取请求头中的token
        String token = request.getHeader("token");

        // 3. 判断token是否存在
        if (StrUtil.isBlank(token)) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED);
        }


        // 6. 校验Redis中是否存在（不存在就是过期了）
        String key = Constant.TOKEN_PREFIX + UserContext.getUserId();
        String tokenInRedis = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isBlank(tokenInRedis)){
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED);
        }

        // 4. 判断token是否被封禁
        if(Constant.TOKEN_BANNED_VALUE.equals(tokenInRedis)) {
            log.warn("拦截到已被强制封禁的用户请求，将其踢出，用户ID: {}", UserContext.getUserId());
            throw new BusinessException(ResultCodeEnum.USER_BANNED);
        }


        // 5. 解析令牌+存入ThreadLocal
        try {
            Claims claims = JwtUtil.parseJwt(token);
            UserContext.setData(claims);
        } catch (Exception e) {
            log.error("非法令牌解析失败：{}", e.getMessage());
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED);
        }

        // 8. 角色权限拦截
        String path = request.getRequestURI();
        Integer roleCode = UserContext.getUserRole();
        RoleEnum role = RoleEnum.fromCode(roleCode);

        if (path.contains("/admin") && role != RoleEnum.ADMIN) {
            throw new BusinessException(ResultCodeEnum.FORBIDDEN);
        }
        // 7. 刷新Token在Redis里的续命时间
        stringRedisTemplate.expire(key, Constant.TOKEN_EXPIRATION, TimeUnit.MINUTES);
        log.info("Token续期成功，用户ID: {}", UserContext.getUserId());

        // 9. 记录用户最后活跃时间到 Redis Hash 中
        UserActiveLog userActiveLog = new UserActiveLog();
        userActiveLog.setUserId(UserContext.getUserId());
        userActiveLog.setRole(UserContext.getUserRole());
        userActiveLog.setActiveTime(LocalDateTime.now());
        stringRedisTemplate.opsForHash().put(
                ACTIVE_TIME_KEY,
                UserContext.getUserId().toString(),
                JSONUtil.toJsonStr(userActiveLog)
        );

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
        UserContext.remove();
    }
}