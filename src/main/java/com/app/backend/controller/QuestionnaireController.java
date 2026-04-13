package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.dto.QuestionnaireCreateRequest;
import com.app.backend.dto.QuestionnaireDto;
import com.app.backend.dto.QuestionnaireUpdateRequest;
import com.app.backend.service.AccessService;
import com.app.backend.service.QuestionnaireService;
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
@RequestMapping("/api/questionnaires")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;
    private final AccessService accessService;

    public QuestionnaireController(QuestionnaireService questionnaireService, AccessService accessService) {
        this.questionnaireService = questionnaireService;
        this.accessService = accessService;
    }

    @PostMapping
    public ApiResponse<QuestionnaireDto> create(@Valid @RequestBody QuestionnaireCreateRequest req, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(questionnaireService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<QuestionnaireDto> update(@PathVariable Long id, @Valid @RequestBody QuestionnaireUpdateRequest req, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(questionnaireService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        questionnaireService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<QuestionnaireDto> detail(@PathVariable Long id, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(questionnaireService.detail(id));
    }

    @GetMapping
    public ApiResponse<IPage<QuestionnaireDto>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request
    ) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(questionnaireService.page(page, size, patientId, startTime, endTime));
    }
}
