package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.dto.LoginRequest;
import com.app.backend.dto.RegisterRequest;
import com.app.backend.dto.UserDto;
import com.app.backend.service.AuthService;
import javax.servlet.http.HttpSession;
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
    public ApiResponse<UserDto> login(@Valid @RequestBody LoginRequest req, HttpSession session) {
        return ApiResponse.ok(authService.login(req, session));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        authService.logout(session);
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    public ApiResponse<UserDto> me(HttpSession session) {
        return ApiResponse.ok(authService.currentUser(session));
    }
}
