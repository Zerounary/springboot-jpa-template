package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.dto.SparkTrainRequest;
import com.app.backend.dto.SparkTrainResultDto;
import com.app.backend.service.SparkMlService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/spark")
public class SparkController {

    private final SparkMlService sparkMlService;

    public SparkController(SparkMlService sparkMlService) {
        this.sparkMlService = sparkMlService;
    }

    @PostMapping("/train-test")
    public ApiResponse<SparkTrainResultDto> trainTest(@RequestBody(required = false) SparkTrainRequest req) {
        return ApiResponse.ok(sparkMlService.trainTest(req));
    }
}
