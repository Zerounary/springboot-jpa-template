package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.dto.PhysicalExamCreateRequest;
import com.app.backend.dto.PhysicalExamDto;
import com.app.backend.dto.PhysicalExamUpdateRequest;
import com.app.backend.service.AccessService;
import com.app.backend.service.PatientService;
import com.app.backend.service.PhysicalExamService;
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
@RequestMapping("/api/physical-exams")
public class PhysicalExamController {

    private final PhysicalExamService physicalExamService;
    private final PatientService patientService;
    private final AccessService accessService;

    public PhysicalExamController(PhysicalExamService physicalExamService, PatientService patientService, AccessService accessService) {
        this.physicalExamService = physicalExamService;
        this.patientService = patientService;
        this.accessService = accessService;
    }

    @PostMapping
    public ApiResponse<PhysicalExamDto> create(@Valid @RequestBody PhysicalExamCreateRequest req, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(physicalExamService.create(req));
    }

    @GetMapping("/mine")
    public ApiResponse<IPage<PhysicalExamDto>> mine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request
    ) {
        accessService.requirePatient(request);
        Long patientId = patientService.getByAccountId(accessService.currentUserId(request)).getId();
        return ApiResponse.ok(physicalExamService.pageByPatientId(page, size, patientId, startTime, endTime));
    }

    @PutMapping("/{id}")
    public ApiResponse<PhysicalExamDto> update(@PathVariable Long id, @Valid @RequestBody PhysicalExamUpdateRequest req, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(physicalExamService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        physicalExamService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<PhysicalExamDto> detail(@PathVariable Long id, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(physicalExamService.detail(id));
    }

    @GetMapping
    public ApiResponse<IPage<PhysicalExamDto>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request
    ) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(physicalExamService.page(page, size, patientId, startTime, endTime));
    }
}
