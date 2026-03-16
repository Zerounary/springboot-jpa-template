package com.app.backend.config;

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
        User admin = userRepository.selectOne(new QueryWrapper<User>().eq("username", "admin").last("LIMIT 1"));
        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordService.hash("admin123"));
            admin.setNickname("管理员");
            userRepository.insert(admin);
            return;
        }
        if (admin.getPasswordHash() == null || "INIT".equals(admin.getPasswordHash())) {
            admin.setPasswordHash(passwordService.hash("admin123"));
            userRepository.updateById(admin);
        }
    }
}
