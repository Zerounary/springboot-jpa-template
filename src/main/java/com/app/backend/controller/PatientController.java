package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.config.AuthInterceptor;
import com.app.backend.dto.PatientCreateRequest;
import com.app.backend.dto.PatientDto;
import com.app.backend.dto.PatientUpdateRequest;
import com.app.backend.service.PatientService;
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
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    public ApiResponse<PatientDto> create(HttpServletRequest request, @Valid @RequestBody PatientCreateRequest req) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(patientService.create(userId, req));
    }

    @PutMapping("/{id}")
    public ApiResponse<PatientDto> update(HttpServletRequest request, @PathVariable("id") Long id,
                                         @Valid @RequestBody PatientUpdateRequest req) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(patientService.update(userId, id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        patientService.delete(userId, id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<PatientDto> detail(HttpServletRequest request, @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(patientService.detail(userId, id));
    }

    @GetMapping
    public ApiResponse<IPage<PatientDto>> page(HttpServletRequest request,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) String keyword) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(patientService.page(userId, page, size, keyword));
    }

    @GetMapping("/me")
    public ApiResponse<PatientDto> me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(patientService.me(userId));
    }

    @PutMapping("/me")
    public ApiResponse<PatientDto> upsertMe(HttpServletRequest request, @Valid @RequestBody PatientUpdateRequest req) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(patientService.upsertMe(userId, req));
    }
}
