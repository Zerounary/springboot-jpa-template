package com.app.backend.config;

import com.app.backend.common.SessionKeys;
import com.app.backend.common.BizException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new BizException(401, "未登录");
        }
        Object userId = session.getAttribute(SessionKeys.LOGIN_USER_ID);
        if (userId == null) {
            throw new BizException(401, "未登录");
        }
        return true;
    }
}
