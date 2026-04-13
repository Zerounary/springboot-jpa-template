package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.dto.PredictionGenerateRequest;
import com.app.backend.dto.PredictionResultCreateRequest;
import com.app.backend.dto.PredictionResultDto;
import com.app.backend.dto.PredictionResultUpdateRequest;
import com.app.backend.service.AccessService;
import com.app.backend.service.PatientService;
import com.app.backend.service.PredictionResultService;
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
@RequestMapping("/api/prediction-results")
public class PredictionResultController {

    private final PredictionResultService predictionResultService;
    private final PatientService patientService;
    private final AccessService accessService;

    public PredictionResultController(PredictionResultService predictionResultService, PatientService patientService, AccessService accessService) {
        this.predictionResultService = predictionResultService;
        this.patientService = patientService;
        this.accessService = accessService;
    }

    @PostMapping
    public ApiResponse<PredictionResultDto> create(@Valid @RequestBody PredictionResultCreateRequest req, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(predictionResultService.create(req));
    }

    @PostMapping("/generate")
    public ApiResponse<PredictionResultDto> generate(@Valid @RequestBody PredictionGenerateRequest req, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(predictionResultService.generateByPatientId(req.getPatientId(), req.getModelId()));
    }

    @GetMapping("/mine")
    public ApiResponse<IPage<PredictionResultDto>> mine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        accessService.requirePatient(request);
        Long patientId = patientService.getByAccountId(accessService.currentUserId(request)).getId();
        return ApiResponse.ok(predictionResultService.pageByPatientId(page, size, patientId));
    }

    @PutMapping("/{id}")
    public ApiResponse<PredictionResultDto> update(@PathVariable Long id, @Valid @RequestBody PredictionResultUpdateRequest req, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(predictionResultService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        predictionResultService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<PredictionResultDto> detail(@PathVariable Long id, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(predictionResultService.detail(id));
    }

    @GetMapping
    public ApiResponse<IPage<PredictionResultDto>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long patientId,
            HttpServletRequest request
    ) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(predictionResultService.page(page, size, patientId));
    }
}
