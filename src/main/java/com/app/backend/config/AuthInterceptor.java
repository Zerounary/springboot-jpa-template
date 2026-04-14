package com.app.backend.config;

import com.app.backend.common.BizException;
import com.app.backend.service.JwtService;
import com.auth0.jwt.interfaces.DecodedJWT;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    public static final String REQ_ATTR_USER_ID = "AUTH_USER_ID";

    private final JwtService jwtService;

    public AuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public static Long getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute(REQ_ATTR_USER_ID);
        if (userId instanceof Long) {
            return (Long) userId;
        }
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        if (userId instanceof String) {
            try {
                return Long.valueOf((String) userId);
            } catch (NumberFormatException ex) {
                throw new BizException(401, "未登录");
            }
        }
        throw new BizException(401, "未登录");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BizException(401, "未登录");
        }

        String token = auth.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            throw new BizException(401, "未登录");
        }

        DecodedJWT jwt = jwtService.verify(token);
        Long userId = jwt.getClaim("uid").asLong();
        if (userId == null) {
            throw new BizException(401, "未登录");
        }

        request.setAttribute(REQ_ATTR_USER_ID, userId);
        return true;
    }
}
