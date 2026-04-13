package com.app.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.app.backend.common.BizException;
import com.app.backend.common.UserRole;
import com.app.backend.dto.UserCreateRequest;
import com.app.backend.dto.UserDto;
import com.app.backend.dto.UserUpdateRequest;
import com.app.backend.entity.User;
import com.app.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public UserService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    @Transactional
    public UserDto create(UserCreateRequest req) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("username", req.getUsername());
        if (userRepository.selectCount(qw) > 0) {
            throw new BizException(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordService.hash(req.getPassword()));
        user.setRole(UserRole.from(req.getRole()).name());
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        userRepository.insert(user);
        return toDto(user);
    }

    @Transactional
    public UserDto update(Long id, UserUpdateRequest req) {
        User user = userRepository.selectById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        if (req.getPassword() != null) {
            user.setPasswordHash(passwordService.hash(req.getPassword()));
        }
        userRepository.updateById(user);
        return toDto(user);
    }

    @Transactional
    public void delete(Long id) {
        if (userRepository.selectById(id) == null) {
            throw new BizException(404, "用户不存在");
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public UserDto detail(Long id) {
        User user = userRepository.selectById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return toDto(user);
    }

    @Transactional(readOnly = true)
    public IPage<UserDto> page(int page, int size, String keyword) {
        Page<User> p = new Page<>(Math.max(page, 0) + 1L, Math.min(Math.max(size, 1), 200));
        QueryWrapper<User> qw = new QueryWrapper<>();
        if (keyword != null) {
            qw.and(w -> w.like("username", keyword).or().like("nickname", keyword));
        }
        qw.orderByDesc("id");
        IPage<User> result = userRepository.selectPage(p, qw);
        return result.convert(this::toDto);
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        User user = userRepository.selectById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("username", username);
        return Optional.ofNullable(userRepository.selectOne(qw));
    }

    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
