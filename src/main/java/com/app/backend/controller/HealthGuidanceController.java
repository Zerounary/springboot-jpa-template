package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.dto.HealthGuidanceCreateRequest;
import com.app.backend.dto.HealthGuidanceDto;
import com.app.backend.dto.HealthGuidanceUpdateRequest;
import com.app.backend.service.AccessService;
import com.app.backend.service.HealthGuidanceService;
import com.app.backend.service.PatientService;
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
@RequestMapping("/api/health-guidances")
public class HealthGuidanceController {

    private final HealthGuidanceService healthGuidanceService;
    private final PatientService patientService;
    private final AccessService accessService;

    public HealthGuidanceController(
            HealthGuidanceService healthGuidanceService,
            PatientService patientService,
            AccessService accessService
    ) {
        this.healthGuidanceService = healthGuidanceService;
        this.patientService = patientService;
        this.accessService = accessService;
    }

    @PostMapping
    public ApiResponse<HealthGuidanceDto> create(@Valid @RequestBody HealthGuidanceCreateRequest req, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(healthGuidanceService.create(accessService.currentUserId(request), req));
    }

    @PutMapping("/{id}")
    public ApiResponse<HealthGuidanceDto> update(@PathVariable Long id, @Valid @RequestBody HealthGuidanceUpdateRequest req, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(healthGuidanceService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        healthGuidanceService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<HealthGuidanceDto> detail(@PathVariable Long id, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(healthGuidanceService.detail(id));
    }

    @GetMapping
    public ApiResponse<IPage<HealthGuidanceDto>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request
    ) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(healthGuidanceService.page(page, size, patientId, startTime, endTime));
    }

    @GetMapping("/mine")
    public ApiResponse<IPage<HealthGuidanceDto>> mine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request
    ) {
        accessService.requirePatient(request);
        Long patientId = patientService.getByAccountId(accessService.currentUserId(request)).getId();
        return ApiResponse.ok(healthGuidanceService.pageByPatientId(page, size, patientId, startTime, endTime));
    }
}
