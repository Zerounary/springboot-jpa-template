package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.config.AuthInterceptor;
import com.app.backend.dto.HealthMonitorCreateRequest;
import com.app.backend.dto.HealthMonitorDto;
import com.app.backend.dto.HealthMonitorUpdateRequest;
import com.app.backend.service.HealthMonitorService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/health-monitors")
public class HealthMonitorController {

    private final HealthMonitorService healthMonitorService;

    public HealthMonitorController(HealthMonitorService healthMonitorService) {
        this.healthMonitorService = healthMonitorService;
    }

    @PostMapping
    public ApiResponse<HealthMonitorDto> create(HttpServletRequest request, @Valid @RequestBody HealthMonitorCreateRequest req) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(healthMonitorService.create(userId, req));
    }

    @PutMapping("/{id}")
    public ApiResponse<HealthMonitorDto> update(HttpServletRequest request, @PathVariable("id") Long id,
                                               @Valid @RequestBody HealthMonitorUpdateRequest req) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(healthMonitorService.update(userId, id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        healthMonitorService.delete(userId, id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<HealthMonitorDto> detail(HttpServletRequest request, @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(healthMonitorService.detail(userId, id));
    }

    @GetMapping
    public ApiResponse<IPage<HealthMonitorDto>> page(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime dateTo
    ) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(healthMonitorService.page(userId, page, size, patientId, dateFrom, dateTo));
    }
}
