package com.app.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.app.backend.common.ApiResponse;
import com.app.backend.service.UserService;
import com.app.backend.dto.UserCreateRequest;
import com.app.backend.dto.UserDto;
import com.app.backend.dto.UserUpdateRequest;

import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ApiResponse<UserDto> create(@Valid @RequestBody UserCreateRequest req) {
        return ApiResponse.ok(userService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserDto> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest req) {
        return ApiResponse.ok(userService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDto> detail(@PathVariable Long id) {
        return ApiResponse.ok(userService.detail(id));
    }

    @GetMapping
    public ApiResponse<IPage<UserDto>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(userService.page(page, size, keyword));
    }
}
