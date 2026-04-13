package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.dto.MlModelDto;
import com.app.backend.service.AccessService;
import com.app.backend.service.MlModelService;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ml-models")
public class MlModelController {

    private final MlModelService mlModelService;
    private final AccessService accessService;

    public MlModelController(MlModelService mlModelService, AccessService accessService) {
        this.mlModelService = mlModelService;
        this.accessService = accessService;
    }

    @GetMapping
    public ApiResponse<List<MlModelDto>> list(@RequestParam(required = false) Boolean activeOnly, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(mlModelService.list(activeOnly));
    }

    @GetMapping("/{id}")
    public ApiResponse<MlModelDto> detail(@PathVariable Long id, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(mlModelService.detail(id));
    }
}
