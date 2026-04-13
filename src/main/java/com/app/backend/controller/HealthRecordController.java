package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.dto.HealthRecordCreateRequest;
import com.app.backend.dto.HealthRecordDto;
import com.app.backend.dto.HealthRecordUpdateRequest;
import com.app.backend.service.AccessService;
import com.app.backend.service.HealthRecordService;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
@RequestMapping("/api/health-records")
public class HealthRecordController {

    private final HealthRecordService healthRecordService;
    private final AccessService accessService;

    public HealthRecordController(HealthRecordService healthRecordService, AccessService accessService) {
        this.healthRecordService = healthRecordService;
        this.accessService = accessService;
    }

    @PostMapping
    public ApiResponse<HealthRecordDto> create(@Valid @RequestBody HealthRecordCreateRequest req, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(healthRecordService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<HealthRecordDto> update(@PathVariable Long id, @Valid @RequestBody HealthRecordUpdateRequest req, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(healthRecordService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        healthRecordService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<HealthRecordDto> detail(@PathVariable Long id, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(healthRecordService.detail(id));
    }

    @GetMapping
    public ApiResponse<IPage<HealthRecordDto>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request
    ) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(healthRecordService.page(page, size, patientId, keyword, startTime, endTime));
    }
}
