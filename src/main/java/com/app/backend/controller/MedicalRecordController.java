package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.config.AuthInterceptor;
import com.app.backend.dto.MedicalRecordCreateRequest;
import com.app.backend.dto.MedicalRecordDto;
import com.app.backend.dto.MedicalRecordUpdateRequest;
import com.app.backend.service.MedicalRecordService;
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
@RequestMapping("/api/medical-records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @PostMapping
    public ApiResponse<MedicalRecordDto> create(HttpServletRequest request, @Valid @RequestBody MedicalRecordCreateRequest req) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(medicalRecordService.create(userId, req));
    }

    @PutMapping("/{id}")
    public ApiResponse<MedicalRecordDto> update(HttpServletRequest request, @PathVariable("id") Long id,
                                               @Valid @RequestBody MedicalRecordUpdateRequest req) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(medicalRecordService.update(userId, id, req));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<MedicalRecordDto> complete(HttpServletRequest request, @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(medicalRecordService.complete(userId, id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        medicalRecordService.delete(userId, id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<MedicalRecordDto> detail(HttpServletRequest request, @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(medicalRecordService.detail(userId, id));
    }

    @GetMapping
    public ApiResponse<IPage<MedicalRecordDto>> page(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long registrationId,
            @RequestParam(required = false) Integer recordStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime visitFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime visitTo
    ) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(medicalRecordService.page(userId, page, size, patientId, doctorId, deptId, registrationId, recordStatus, keyword, visitFrom, visitTo));
    }
}
