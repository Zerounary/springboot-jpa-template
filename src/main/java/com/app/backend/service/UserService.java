package com.app.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.app.backend.common.BizException;
import com.app.backend.dto.UserCreateRequest;
import com.app.backend.dto.UserDto;
import com.app.backend.dto.UserUpdateRequest;
import com.app.backend.entity.User;
import com.app.backend.repository.UserRepository;
import com.app.backend.service.PasswordService;
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
        qw.eq("is_deleted", 0);
        if (userRepository.selectCount(qw) > 0) {
            throw new BizException(400, "用户名已存在");
        }

        if (req.getPhone() != null) {
            QueryWrapper<User> pw = new QueryWrapper<>();
            pw.eq("phone", req.getPhone());
            pw.eq("is_deleted", 0);
            if (userRepository.selectCount(pw) > 0) {
                throw new BizException(400, "手机号已存在");
            }
        }

        if (req.getIdCard() != null) {
            QueryWrapper<User> iw = new QueryWrapper<>();
            iw.eq("id_card", req.getIdCard());
            iw.eq("is_deleted", 0);
            if (userRepository.selectCount(iw) > 0) {
                throw new BizException(400, "身份证号已存在");
            }
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordService.hash(req.getPassword()));
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());

        user.setRealName(req.getRealName() != null ? req.getRealName() : (req.getNickname() != null ? req.getNickname() : req.getUsername()));
        user.setPhone(req.getPhone());
        user.setIdCard(req.getIdCard());
        user.setGender(req.getGender());
        user.setRoleType(req.getRoleType() != null ? req.getRoleType() : 3);
        user.setStatus(req.getStatus() != null ? req.getStatus() : 1);
        user.setAvatar(req.getAvatar());
        user.setIsDeleted(0);

        userRepository.insert(user);
        return toDto(user);
    }

    @Transactional
    public UserDto update(Long id, UserUpdateRequest req) {
        User user = userRepository.selectById(id);
        if (user == null || (user.getIsDeleted() != null && user.getIsDeleted() != 0)) {
            throw new BizException(404, "用户不存在");
        }

        if (req.getPhone() != null) {
            QueryWrapper<User> pw = new QueryWrapper<>();
            pw.eq("phone", req.getPhone());
            pw.eq("is_deleted", 0);
            pw.ne("id", id);
            if (userRepository.selectCount(pw) > 0) {
                throw new BizException(400, "手机号已存在");
            }
        }

        if (req.getIdCard() != null) {
            QueryWrapper<User> iw = new QueryWrapper<>();
            iw.eq("id_card", req.getIdCard());
            iw.eq("is_deleted", 0);
            iw.ne("id", id);
            if (userRepository.selectCount(iw) > 0) {
                throw new BizException(400, "身份证号已存在");
            }
        }

        if (req.getNickname() != null) {
            user.setNickname(req.getNickname());
        }
        if (req.getEmail() != null) {
            user.setEmail(req.getEmail());
        }
        if (req.getPassword() != null) {
            user.setPasswordHash(passwordService.hash(req.getPassword()));
        }

        if (req.getRealName() != null) {
            user.setRealName(req.getRealName());
        }
        if (req.getPhone() != null) {
            user.setPhone(req.getPhone());
        }
        if (req.getIdCard() != null) {
            user.setIdCard(req.getIdCard());
        }
        if (req.getGender() != null) {
            user.setGender(req.getGender());
        }
        if (req.getRoleType() != null) {
            user.setRoleType(req.getRoleType());
        }
        if (req.getStatus() != null) {
            user.setStatus(req.getStatus());
        }
        if (req.getAvatar() != null) {
            user.setAvatar(req.getAvatar());
        }

        userRepository.updateById(user);
        return toDto(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.selectById(id);
        if (user == null || (user.getIsDeleted() != null && user.getIsDeleted() != 0)) {
            throw new BizException(404, "用户不存在");
        }
        user.setIsDeleted(1);
        userRepository.updateById(user);
    }

    @Transactional(readOnly = true)
    public UserDto detail(Long id) {
        User user = userRepository.selectById(id);
        if (user == null || (user.getIsDeleted() != null && user.getIsDeleted() != 0)) {
            throw new BizException(404, "用户不存在");
        }
        return toDto(user);
    }

    @Transactional(readOnly = true)
    public IPage<UserDto> page(int page, int size, String keyword) {
        Page<User> p = new Page<>(Math.max(page, 0) + 1L, Math.min(Math.max(size, 1), 200));
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
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
        if (user == null || (user.getIsDeleted() != null && user.getIsDeleted() != 0)) {
            throw new BizException(404, "用户不存在");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("username", username);
        qw.eq("is_deleted", 0);
        qw.eq("status", 1);
        return Optional.ofNullable(userRepository.selectOne(qw));
    }

    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setRealName(user.getRealName());
        dto.setPhone(user.getPhone());
        dto.setIdCard(user.getIdCard());
        dto.setGender(user.getGender());
        dto.setRoleType(user.getRoleType());
        dto.setStatus(user.getStatus());
        dto.setAvatar(user.getAvatar());
        dto.setIsDeleted(user.getIsDeleted());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
