package com.app.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.app.backend.common.ApiResponse;
import com.app.backend.service.AccessService;
import com.app.backend.service.UserService;
import com.app.backend.dto.UserCreateRequest;
import com.app.backend.dto.UserDto;
import com.app.backend.dto.UserUpdateRequest;

import javax.servlet.http.HttpServletRequest;
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
    private final AccessService accessService;

    public UserController(UserService userService, AccessService accessService) {
        this.userService = userService;
        this.accessService = accessService;
    }

    @PostMapping
    public ApiResponse<UserDto> create(@Valid @RequestBody UserCreateRequest req, HttpServletRequest request) {
        accessService.requireAdmin(request);
        return ApiResponse.ok(userService.create(req));
    }

    @GetMapping("/me")
    public ApiResponse<UserDto> me(HttpServletRequest request) {
        return ApiResponse.ok(userService.detail(accessService.currentUserId(request)));
    }

    @PutMapping("/me")
    public ApiResponse<UserDto> updateMe(@Valid @RequestBody UserUpdateRequest req, HttpServletRequest request) {
        return ApiResponse.ok(userService.update(accessService.currentUserId(request), req));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserDto> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest req, HttpServletRequest request) {
        accessService.requireAdmin(request);
        return ApiResponse.ok(userService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        accessService.requireAdmin(request);
        userService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDto> detail(@PathVariable Long id, HttpServletRequest request) {
        accessService.requireAdmin(request);
        return ApiResponse.ok(userService.detail(id));
    }

    @GetMapping
    public ApiResponse<IPage<UserDto>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request
    ) {
        accessService.requireAdmin(request);
        return ApiResponse.ok(userService.page(page, size, keyword));
    }
}
