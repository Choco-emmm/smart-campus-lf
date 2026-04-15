package com.choco.smartlf.interceptor;

import com.choco.smartlf.enums.ResultCodeEnum;
import com.choco.smartlf.enums.RoleEnum;
import com.choco.smartlf.exception.BusinessException;
import com.choco.smartlf.service.impl.TokenAuthService;
import com.choco.smartlf.utils.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

    private final TokenAuthService tokenAuthService; // 注入核心鉴权服务

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("token");

        // 🌟 如果 Header 里没有，再尝试从 URL 参数中获取 token（专门为 SSE 准备）
        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
        }

        // 1. 调用统一鉴权服务 (这一步里面已经完成了续期、查封禁、记活跃等所有操作)
        Claims claims = tokenAuthService.authenticateAndRenew(token);

        // 2. 存入 ThreadLocal
        UserContext.setData(claims);

        // 3. 专属的接口路径权限拦截 (这个只有 HTTP 请求才需要，保留在这里)
        String path = request.getRequestURI();
        Integer roleCode = UserContext.getUserRole();

        if (path.contains("/admin") && !RoleEnum.ADMIN.getCode().equals(roleCode)) {
            throw new BusinessException(ResultCodeEnum.FORBIDDEN);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
        UserContext.remove();
    }
}