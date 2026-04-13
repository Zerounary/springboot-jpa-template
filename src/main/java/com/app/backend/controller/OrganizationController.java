package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.dto.OrganizationCreateRequest;
import com.app.backend.dto.OrganizationDto;
import com.app.backend.dto.OrganizationUpdateRequest;
import com.app.backend.service.AccessService;
import com.app.backend.service.OrganizationService;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;
    private final AccessService accessService;

    public OrganizationController(OrganizationService organizationService, AccessService accessService) {
        this.organizationService = organizationService;
        this.accessService = accessService;
    }

    @GetMapping
    public ApiResponse<List<OrganizationDto>> list(@RequestParam(required = false) String keyword, HttpServletRequest request) {
        accessService.currentUserId(request);
        return ApiResponse.ok(organizationService.list(keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrganizationDto> detail(@PathVariable Long id, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(organizationService.detail(id));
    }

    @PostMapping
    public ApiResponse<OrganizationDto> create(@Valid @RequestBody OrganizationCreateRequest req, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(organizationService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<OrganizationDto> update(@PathVariable Long id, @Valid @RequestBody OrganizationUpdateRequest req, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        return ApiResponse.ok(organizationService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        accessService.requireDoctorOrAdmin(request);
        organizationService.delete(id);
        return ApiResponse.ok();
    }
}
