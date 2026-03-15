package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.config.AuthInterceptor;
import com.app.backend.dto.SystemNewsCreateRequest;
import com.app.backend.dto.SystemNewsDto;
import com.app.backend.dto.SystemNewsUpdateRequest;
import com.app.backend.service.SystemNewsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/system-news")
public class SystemNewsController {

    private final SystemNewsService systemNewsService;

    public SystemNewsController(SystemNewsService systemNewsService) {
        this.systemNewsService = systemNewsService;
    }

    @PostMapping
    public ApiResponse<SystemNewsDto> create(HttpServletRequest request, @Valid @RequestBody SystemNewsCreateRequest req) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(systemNewsService.create(userId, req));
    }

    @PutMapping("/{id}")
    public ApiResponse<SystemNewsDto> update(HttpServletRequest request, @PathVariable("id") Long id,
                                            @Valid @RequestBody SystemNewsUpdateRequest req) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(systemNewsService.update(userId, id, req));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<SystemNewsDto> publish(HttpServletRequest request, @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(systemNewsService.publish(userId, id));
    }

    @PostMapping("/{id}/unpublish")
    public ApiResponse<SystemNewsDto> unpublish(HttpServletRequest request, @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(systemNewsService.unpublish(userId, id));
    }

    @PostMapping("/{id}/top")
    public ApiResponse<SystemNewsDto> top(HttpServletRequest request, @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(systemNewsService.top(userId, id));
    }

    @PostMapping("/{id}/untop")
    public ApiResponse<SystemNewsDto> untop(HttpServletRequest request, @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(systemNewsService.untop(userId, id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        systemNewsService.delete(userId, id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<SystemNewsDto> detail(HttpServletRequest request, @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(systemNewsService.detail(userId, id));
    }

    @GetMapping
    public ApiResponse<IPage<SystemNewsDto>> page(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isTop,
            @RequestParam(defaultValue = "false") boolean includeContent
    ) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(systemNewsService.page(userId, page, size, title, status, isTop, includeContent));
    }
}
