package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.dto.HealthRecordCreateRequest;
import com.app.backend.dto.HealthRecordDto;
import com.app.backend.dto.HealthRecordUpdateRequest;
import com.app.backend.service.HealthRecordService;
import com.baomidou.mybatisplus.core.metadata.IPage;
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

    public HealthRecordController(HealthRecordService healthRecordService) {
        this.healthRecordService = healthRecordService;
    }

    @PostMapping
    public ApiResponse<HealthRecordDto> create(@Valid @RequestBody HealthRecordCreateRequest req) {
        return ApiResponse.ok(healthRecordService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<HealthRecordDto> update(@PathVariable Long id, @Valid @RequestBody HealthRecordUpdateRequest req) {
        return ApiResponse.ok(healthRecordService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        healthRecordService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<HealthRecordDto> detail(@PathVariable Long id) {
        return ApiResponse.ok(healthRecordService.detail(id));
    }

    @GetMapping
    public ApiResponse<IPage<HealthRecordDto>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(healthRecordService.page(page, size, patientId, keyword));
    }
}
