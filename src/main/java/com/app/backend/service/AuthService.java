package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.common.UserRole;
import com.app.backend.dto.LoginRequest;
import com.app.backend.dto.RegisterRequest;
import com.app.backend.dto.UserCreateRequest;
import com.app.backend.dto.UserDto;
import com.app.backend.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public AuthService(UserService userService, PasswordService passwordService, JwtService jwtService) {
        this.userService = userService;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserDto register(RegisterRequest req) {
        UserRole role = UserRole.from(req.getRole());
        if (role.isAdmin()) {
            throw new BizException(403, "不允许注册管理员账号");
        }
        return userService.create(toUserCreateRequest(req));
    }

    @Transactional
    public String login(LoginRequest req) {
        User user = userService.findByUsername(req.getUsername())
                .orElseThrow(() -> new BizException(400, "用户名或密码错误"));
        if (!passwordService.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BizException(400, "用户名或密码错误");
        }
        return jwtService.createToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Transactional(readOnly = true)
    public UserDto currentUser(Long userId) {
        if (userId == null) {
            throw new BizException(401, "未登录");
        }
        return userService.detail(userId);
    }

    private UserCreateRequest toUserCreateRequest(RegisterRequest req) {
        UserCreateRequest u = new UserCreateRequest();
        u.setUsername(req.getUsername());
        u.setPassword(req.getPassword());
        u.setNickname(req.getNickname());
        u.setRole(req.getRole());
        return u;
    }
}
