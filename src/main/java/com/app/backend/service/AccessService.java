package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.common.UserRole;
import com.app.backend.config.AuthInterceptor;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class AccessService {

    public Long currentUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        if (userId == null) {
            throw new BizException(401, "未登录");
        }
        return userId;
    }

    public UserRole currentRole(HttpServletRequest request) {
        Object roleValue = request.getAttribute(AuthInterceptor.REQ_ATTR_ROLE);
        if (roleValue instanceof UserRole) {
            return (UserRole) roleValue;
        }
        if (roleValue instanceof String) {
            return UserRole.from((String) roleValue);
        }
        throw new BizException(401, "未登录");
    }

    public void requireAdmin(HttpServletRequest request) {
        if (!currentRole(request).isAdmin()) {
            throw new BizException(403, "无权限");
        }
    }

    public void requireDoctorOrAdmin(HttpServletRequest request) {
        UserRole role = currentRole(request);
        if (!(role.isDoctor() || role.isAdmin())) {
            throw new BizException(403, "无权限");
        }
    }

    public void requirePatient(HttpServletRequest request) {
        if (!currentRole(request).isPatient()) {
            throw new BizException(403, "无权限");
        }
    }
}
