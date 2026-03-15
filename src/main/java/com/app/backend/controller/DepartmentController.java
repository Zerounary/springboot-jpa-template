package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.config.AuthInterceptor;
import com.app.backend.dto.DepartmentCreateRequest;
import com.app.backend.dto.DepartmentDto;
import com.app.backend.dto.DepartmentTreeDto;
import com.app.backend.dto.DepartmentUpdateRequest;
import com.app.backend.service.DepartmentService;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    public ApiResponse<DepartmentDto> create(HttpServletRequest request, @Valid @RequestBody DepartmentCreateRequest req) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(departmentService.create(userId, req));
    }

    @PutMapping("/{id}")
    public ApiResponse<DepartmentDto> update(HttpServletRequest request, @PathVariable("id") Long id,
                                            @Valid @RequestBody DepartmentUpdateRequest req) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        return ApiResponse.ok(departmentService.update(userId, id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        departmentService.delete(userId, id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<DepartmentDto> detail(@PathVariable("id") Long id) {
        return ApiResponse.ok(departmentService.detail(id));
    }

    @GetMapping
    public ApiResponse<IPage<DepartmentDto>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
        return ApiResponse.ok(departmentService.page(page, size, keyword, status));
    }

    @GetMapping("/tree")
    public ApiResponse<List<DepartmentTreeDto>> tree(
            @RequestParam(defaultValue = "false") boolean includeDisabled
    ) {
        return ApiResponse.ok(departmentService.tree(includeDisabled));
    }
}
