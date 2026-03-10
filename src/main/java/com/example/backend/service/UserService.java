package com.example.backend.service;

import com.example.backend.common.BizException;
import com.example.backend.dto.UserCreateRequest;
import com.example.backend.dto.UserDto;
import com.example.backend.dto.UserUpdateRequest;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDto create(UserCreateRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BizException(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        User saved = userRepository.save(user);
        return toDto(saved);
    }

    @Transactional
    public UserDto update(Long id, UserUpdateRequest req) {
        User user = userRepository.findById(id).orElseThrow(() -> new BizException(404, "用户不存在"));
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        if (req.getPassword() != null && req.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        return toDto(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new BizException(404, "用户不存在");
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public UserDto detail(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new BizException(404, "用户不存在"));
        return toDto(user);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> page(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200), Sort.by(Sort.Direction.DESC, "id"));
        if (keyword == null) {
            return userRepository.findAll(pageable).map(this::toDto);
        }
        return userRepository.findByUsernameContainingIgnoreCaseOrNicknameContainingIgnoreCase(keyword, keyword, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new BizException(404, "用户不存在"));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
