package com.example.backend.auth;

import com.example.backend.auth.dto.LoginRequest;
import com.example.backend.auth.dto.RegisterRequest;
import com.example.backend.common.BizException;
import com.example.backend.user.User;
import com.example.backend.user.UserService;
import com.example.backend.user.dto.UserDto;
import javax.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDto register(RegisterRequest req) {
        return userService.create(toUserCreateRequest(req));
    }

    @Transactional
    public UserDto login(LoginRequest req, HttpSession session) {
        User user = userService.findByUsername(req.getUsername())
                .orElseThrow(() -> new BizException(400, "用户名或密码错误"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BizException(400, "用户名或密码错误");
        }
        session.setAttribute(SessionKeys.LOGIN_USER_ID, user.getId());
        return userService.toDto(user);
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    @Transactional(readOnly = true)
    public UserDto currentUser(HttpSession session) {
        Object userIdObj = session.getAttribute(SessionKeys.LOGIN_USER_ID);
        if (userIdObj == null) {
            throw new BizException(401, "未登录");
        }
        Long userId = (Long) userIdObj;
        return userService.detail(userId);
    }

    private com.example.backend.user.dto.UserCreateRequest toUserCreateRequest(RegisterRequest req) {
        com.example.backend.user.dto.UserCreateRequest u = new com.example.backend.user.dto.UserCreateRequest();
        u.setUsername(req.getUsername());
        u.setPassword(req.getPassword());
        u.setNickname(req.getNickname());
        return u;
    }
}
