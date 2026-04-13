package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.dto.SparkTrainRequest;
import com.app.backend.dto.SparkTrainResultDto;
import com.app.backend.service.AccessService;
import com.app.backend.service.SparkMlService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/spark")
public class SparkController {

    private final SparkMlService sparkMlService;
    private final AccessService accessService;

    public SparkController(SparkMlService sparkMlService, AccessService accessService) {
        this.sparkMlService = sparkMlService;
        this.accessService = accessService;
    }

    @PostMapping("/train-test")
    public ApiResponse<SparkTrainResultDto> trainTest(@RequestBody(required = false) SparkTrainRequest req, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(sparkMlService.trainTest(req));
    }
}
