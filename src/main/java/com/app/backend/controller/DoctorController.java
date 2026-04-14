package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.config.AuthInterceptor;
import com.app.backend.dto.DoctorCreateRequest;
import com.app.backend.dto.DoctorDto;
import com.app.backend.dto.DoctorUpdateRequest;
import com.app.backend.service.DoctorService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    public ApiResponse<DoctorDto> create(HttpServletRequest request, @Valid @RequestBody DoctorCreateRequest req) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(doctorService.create(userId, req));
    }

    @PutMapping("/{id}")
    public ApiResponse<DoctorDto> update(HttpServletRequest request, @PathVariable("id") Long id,
                                        @Valid @RequestBody DoctorUpdateRequest req) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(doctorService.update(userId, id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        doctorService.delete(userId, id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<DoctorDto> detail(@PathVariable("id") Long id) {
        return ApiResponse.ok(doctorService.detail(id));
    }

    @GetMapping
    public ApiResponse<IPage<DoctorDto>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate scheduleDate
    ) {
        return ApiResponse.ok(doctorService.page(page, size, deptId, keyword, scheduleDate));
    }
}
