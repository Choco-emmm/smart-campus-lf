package com.choco.smartlf.interceptor;

import cn.hutool.core.util.StrUtil;
import com.choco.smartlf.enums.ErrorMsgEnum;
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

import java.util.concurrent.TimeUnit;

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
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, ErrorMsgEnum.TOKEN_NOT_LOGIN);
        }

        // 4. 解析令牌+存入ThreadLocal
        try {
            Claims claims = JwtUtil.parseJwt(token);
            UserContext.setData(claims);
        } catch (Exception e) {
            log.error("非法令牌解析失败：{}", e.getMessage());
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, ErrorMsgEnum.TOKEN_INVALID);
        }

        // 5. 校验Redis中是否过期（单点登录/强制下线防线）
        String key = Constant.TOKEN_PREFIX + UserContext.getUserId();
        if(StrUtil.isBlank(stringRedisTemplate.opsForValue().get(key))){
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, ErrorMsgEnum.TOKEN_EXPIRED);
        }
        // 6. 刷新Token在Redis里的续命时间
        stringRedisTemplate.expire(key, Constant.TOKEN_EXPIRATION, TimeUnit.MINUTES);
        log.info("Token续期成功，用户ID: {}", UserContext.getUserId());

        // 7. 角色权限拦截
        String path = request.getRequestURI();
        Integer roleCode = UserContext.getUserRole();
        RoleEnum role = RoleEnum.fromCode(roleCode);

        if (path.contains("/student") && role != RoleEnum.STUDENT) {
            throw new BusinessException(ResultCodeEnum.FORBIDDEN, ErrorMsgEnum.PERMISSION_DENIED_STUDENT);
        } else if (path.contains("/admin") && role != RoleEnum.ADMIN) {
            throw new BusinessException(ResultCodeEnum.FORBIDDEN, ErrorMsgEnum.PERMISSION_DENIED_ADMIN);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
        UserContext.remove();
    }
}