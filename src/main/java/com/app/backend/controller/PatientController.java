package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.dto.PatientCreateRequest;
import com.app.backend.dto.PatientDto;
import com.app.backend.dto.PatientUpdateRequest;
import com.app.backend.service.AccessService;
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
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final AccessService accessService;

    public PatientController(PatientService patientService, AccessService accessService) {
        this.patientService = patientService;
        this.accessService = accessService;
    }

    @PostMapping
    public ApiResponse<PatientDto> create(@Valid @RequestBody PatientCreateRequest req, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(patientService.create(req));
    }

    @GetMapping("/me")
    public ApiResponse<PatientDto> me(HttpServletRequest request) {
        accessService.requirePatient(request);
        return ApiResponse.ok(patientService.detailByAccountId(accessService.currentUserId(request)));
    }

    @PutMapping("/me")
    public ApiResponse<PatientDto> updateMe(@Valid @RequestBody PatientUpdateRequest req, HttpServletRequest request) {
        accessService.requirePatient(request);
        return ApiResponse.ok(patientService.updateByAccountId(accessService.currentUserId(request), req));
    }

    @PutMapping("/{id}")
    public ApiResponse<PatientDto> update(@PathVariable Long id, @Valid @RequestBody PatientUpdateRequest req, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(patientService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        patientService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<PatientDto> detail(@PathVariable Long id, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(patientService.detail(id));
    }

    @GetMapping
    public ApiResponse<IPage<PatientDto>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request
    ) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(patientService.page(page, size, keyword));
    }
}
