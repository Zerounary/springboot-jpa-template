package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.common.BizException;
import com.app.backend.dto.LoginRequest;
import com.app.backend.dto.RegisterRequest;
import com.app.backend.dto.UserDto;
import com.app.backend.config.AuthInterceptor;
import com.app.backend.service.AuthService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<UserDto> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<String> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    public ApiResponse<UserDto> me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        Integer roleType = (Integer) request.getAttribute(AuthInterceptor.REQ_ATTR_ROLE_TYPE);
        if (userId == null) {
            throw new BizException(401, "未登录");
        }
        return ApiResponse.ok(authService.currentUser(userId, roleType));
    }
}
