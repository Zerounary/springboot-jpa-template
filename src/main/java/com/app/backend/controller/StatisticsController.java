package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.config.AuthInterceptor;
import com.app.backend.service.StatisticsService;
import com.app.backend.service.PatientStatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final PatientStatisticsService patientStatisticsService;

    public StatisticsController(StatisticsService statisticsService,
                               PatientStatisticsService patientStatisticsService) {
        this.statisticsService = statisticsService;
        this.patientStatisticsService = patientStatisticsService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> getDashboardStatistics() {
        return ApiResponse.ok(statisticsService.getDashboardStatistics());
    }

    @GetMapping("/patient")
    public ApiResponse<Map<String, Object>> getPatientStatistics(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(patientStatisticsService.getPatientStatistics(userId));
    }
}
