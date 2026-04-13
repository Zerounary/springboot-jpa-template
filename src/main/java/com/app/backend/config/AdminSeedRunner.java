package com.app.backend.config;

import com.app.backend.common.UserRole;
import com.app.backend.entity.User;
import com.app.backend.repository.UserRepository;
import com.app.backend.service.PasswordService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminSeedRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public AdminSeedRunner(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    @Override
    public void run(String... args) {
        ensureUser("admin", "admin123", "管理员", UserRole.ADMIN);
        ensureUser("doctor", "doctor123", "医生账号", UserRole.DOCTOR);
        ensureUser("patient1001", "patient123", "患者1001", UserRole.PATIENT);
    }

    private void ensureUser(String username, String password, String nickname, UserRole role) {
        User user = userRepository.selectOne(new QueryWrapper<User>().eq("username", username).last("LIMIT 1"));
        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setPasswordHash(passwordService.hash(password));
            user.setRole(role.name());
            user.setNickname(nickname);
            userRepository.insert(user);
            return;
        }
        user.setRole(role.name());
        if (user.getNickname() == null) {
            user.setNickname(nickname);
        }
        if (user.getPasswordHash() == null || "INIT".equals(user.getPasswordHash())) {
            user.setPasswordHash(passwordService.hash(password));
        }
        userRepository.updateById(user);
    }
}
