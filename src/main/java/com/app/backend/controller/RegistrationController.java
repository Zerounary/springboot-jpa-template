package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.config.AuthInterceptor;
import com.app.backend.dto.RegistrationCreateRequest;
import com.app.backend.dto.RegistrationDto;
import com.app.backend.service.RegistrationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.LocalDate;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    public ApiResponse<RegistrationDto> create(HttpServletRequest request, @Valid @RequestBody RegistrationCreateRequest req) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(registrationService.create(userId, req));
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<RegistrationDto> pay(HttpServletRequest request, @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(registrationService.pay(userId, id));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<RegistrationDto> cancel(HttpServletRequest request, @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(registrationService.cancel(userId, id));
    }

    @PostMapping("/{id}/visit")
    public ApiResponse<RegistrationDto> markVisited(HttpServletRequest request, @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(registrationService.markVisited(userId, id));
    }

    @GetMapping("/{id}")
    public ApiResponse<RegistrationDto> detail(HttpServletRequest request, @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(registrationService.detail(userId, id));
    }

    @GetMapping
    public ApiResponse<IPage<RegistrationDto>> page(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer payStatus,
            @RequestParam(required = false) Integer registrationStatus,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo
    ) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(registrationService.page(userId, page, size, payStatus, registrationStatus, deptId, doctorId, patientId, dateFrom, dateTo));
    }
}
