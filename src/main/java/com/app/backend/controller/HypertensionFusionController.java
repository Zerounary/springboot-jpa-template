package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.dto.HypertensionFusionDto;
import com.app.backend.dto.HypertensionFusionSyncAllResultDto;
import com.app.backend.dto.HypertensionFusionSyncRequest;
import com.app.backend.service.HypertensionFusionService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hypertension-fusion")
public class HypertensionFusionController {

    private final HypertensionFusionService fusionService;

    public HypertensionFusionController(HypertensionFusionService fusionService) {
        this.fusionService = fusionService;
    }

    @PostMapping("/sync")
    public ApiResponse<HypertensionFusionDto> sync(@Valid @RequestBody HypertensionFusionSyncRequest req) {
        return ApiResponse.ok(fusionService.syncOne(req.getPatientId(), req.getHypertensionLabel()));
    }

    @PostMapping("/sync-all")
    public ApiResponse<HypertensionFusionSyncAllResultDto> syncAll() {
        return ApiResponse.ok(fusionService.syncAll());
    }

    @GetMapping("/detail")
    public ApiResponse<HypertensionFusionDto> detailByPatientId(@RequestParam Long patientId) {
        return ApiResponse.ok(fusionService.detailByPatientId(patientId));
    }

    @GetMapping
    public ApiResponse<IPage<HypertensionFusionDto>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(fusionService.page(page, size, keyword));
    }
}
