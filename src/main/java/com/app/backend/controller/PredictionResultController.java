package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.dto.PredictionResultCreateRequest;
import com.app.backend.dto.PredictionResultDto;
import com.app.backend.dto.PredictionResultUpdateRequest;
import com.app.backend.service.PredictionResultService;
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
@RequestMapping("/api/prediction-results")
public class PredictionResultController {

    private final PredictionResultService predictionResultService;

    public PredictionResultController(PredictionResultService predictionResultService) {
        this.predictionResultService = predictionResultService;
    }

    @PostMapping
    public ApiResponse<PredictionResultDto> create(@Valid @RequestBody PredictionResultCreateRequest req) {
        return ApiResponse.ok(predictionResultService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<PredictionResultDto> update(@PathVariable Long id, @Valid @RequestBody PredictionResultUpdateRequest req) {
        return ApiResponse.ok(predictionResultService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        predictionResultService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<PredictionResultDto> detail(@PathVariable Long id) {
        return ApiResponse.ok(predictionResultService.detail(id));
    }

    @GetMapping
    public ApiResponse<IPage<PredictionResultDto>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long patientId
    ) {
        return ApiResponse.ok(predictionResultService.page(page, size, patientId));
    }
}
