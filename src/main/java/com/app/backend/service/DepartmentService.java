package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.DepartmentCreateRequest;
import com.app.backend.dto.DepartmentDto;
import com.app.backend.dto.DepartmentTreeDto;
import com.app.backend.dto.DepartmentUpdateRequest;
import com.app.backend.entity.HospitalDepartment;
import com.app.backend.entity.User;
import com.app.backend.repository.HospitalDepartmentRepository;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentService {

    private final HospitalDepartmentRepository departmentRepository;
    private final UserService userService;

    public DepartmentService(HospitalDepartmentRepository departmentRepository, UserService userService) {
        this.departmentRepository = departmentRepository;
        this.userService = userService;
    }

    @Transactional
    public DepartmentDto create(Long operatorUserId, DepartmentCreateRequest req) {
        checkAdmin(operatorUserId);
        checkNameUnique(null, req.getDeptName());
        checkCodeUnique(null, req.getDeptCode());

        HospitalDepartment dept = new HospitalDepartment();
        dept.setDeptName(req.getDeptName());
        dept.setDeptCode(req.getDeptCode());
        dept.setParentId(req.getParentId());
        dept.setDescription(req.getDescription());
        dept.setSort(req.getSort() != null ? req.getSort() : 0);
        dept.setStatus(req.getStatus() != null ? req.getStatus() : 1);
        dept.setIsDeleted(0);
        departmentRepository.insert(dept);
        return toDto(dept);
    }

    @Transactional
    public DepartmentDto update(Long operatorUserId, Long deptId, DepartmentUpdateRequest req) {
        checkAdmin(operatorUserId);
        HospitalDepartment dept = departmentRepository.selectById(deptId);
        if (dept == null || (dept.getIsDeleted() != null && dept.getIsDeleted() != 0)) {
            throw new BizException(404, "科室不存在");
        }

        if (req.getDeptName() != null) {
            checkNameUnique(deptId, req.getDeptName());
            dept.setDeptName(req.getDeptName());
        }
        if (req.getDeptCode() != null) {
            checkCodeUnique(deptId, req.getDeptCode());
            dept.setDeptCode(req.getDeptCode());
        }
        if (req.getParentId() != null) {
            if (deptId.equals(req.getParentId())) {
                throw new BizException(400, "parentId 不能等于 deptId");
            }
            dept.setParentId(req.getParentId());
        }
        if (req.getDescription() != null) {
            dept.setDescription(req.getDescription());
        }
        if (req.getSort() != null) {
            dept.setSort(req.getSort());
        }
        if (req.getStatus() != null) {
            dept.setStatus(req.getStatus());
        }

        departmentRepository.updateById(dept);
        return toDto(dept);
    }

    @Transactional
    public void delete(Long operatorUserId, Long deptId) {
        checkAdmin(operatorUserId);
        HospitalDepartment dept = departmentRepository.selectById(deptId);
        if (dept == null || (dept.getIsDeleted() != null && dept.getIsDeleted() != 0)) {
            throw new BizException(404, "科室不存在");
        }
        dept.setIsDeleted(1);
        departmentRepository.updateById(dept);
    }

    @Transactional(readOnly = true)
    public DepartmentDto detail(Long deptId) {
        HospitalDepartment dept = departmentRepository.selectById(deptId);
        if (dept == null || (dept.getIsDeleted() != null && dept.getIsDeleted() != 0)) {
            throw new BizException(404, "科室不存在");
        }
        return toDto(dept);
    }

    @Transactional(readOnly = true)
    public IPage<DepartmentDto> page(int page, int size, String keyword, Integer status) {
        Page<HospitalDepartment> p = new Page<>(Math.max(page, 0) + 1L, Math.min(Math.max(size, 1), 200));
        QueryWrapper<HospitalDepartment> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        if (status != null) {
            qw.eq("status", status);
        }
        if (keyword != null) {
            qw.and(w -> w.like("dept_name", keyword).or().like("dept_code", keyword));
        }
        qw.orderByAsc("sort").orderByAsc("dept_id");
        return departmentRepository.selectPage(p, qw).convert(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<DepartmentTreeDto> tree(boolean includeDisabled) {
        QueryWrapper<HospitalDepartment> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        if (!includeDisabled) {
            qw.eq("status", 1);
        }
        qw.orderByAsc("sort").orderByAsc("dept_id");

        List<HospitalDepartment> list = departmentRepository.selectList(qw);
        Map<Long, DepartmentTreeDto> map = new HashMap<>();
        for (HospitalDepartment d : list) {
            DepartmentTreeDto node = new DepartmentTreeDto();
            fillDto(node, d);
            map.put(d.getDeptId(), node);
        }

        List<DepartmentTreeDto> roots = new ArrayList<>();
        for (HospitalDepartment d : list) {
            DepartmentTreeDto node = map.get(d.getDeptId());
            Long pid = d.getParentId();
            if (pid == null || pid == 0) {
                roots.add(node);
                continue;
            }
            DepartmentTreeDto parent = map.get(pid);
            if (parent == null) {
                roots.add(node);
                continue;
            }
            parent.getChildren().add(node);
        }
        return roots;
    }

    private void checkAdmin(Long operatorUserId) {
        if (operatorUserId == null) {
            throw new BizException(401, "未登录");
        }
        User u = userService.getById(operatorUserId);
        if (u.getRoleType() == null || u.getRoleType() != 1) {
            throw new BizException(403, "无权限");
        }
    }

    private void checkNameUnique(Long deptId, String deptName) {
        QueryWrapper<HospitalDepartment> qw = new QueryWrapper<>();
        qw.eq("dept_name", deptName);
        qw.eq("is_deleted", 0);
        if (deptId != null) {
            qw.ne("dept_id", deptId);
        }
        if (departmentRepository.selectCount(qw) > 0) {
            throw new BizException(400, "科室名称已存在");
        }
    }

    private void checkCodeUnique(Long deptId, String deptCode) {
        QueryWrapper<HospitalDepartment> qw = new QueryWrapper<>();
        qw.eq("dept_code", deptCode);
        qw.eq("is_deleted", 0);
        if (deptId != null) {
            qw.ne("dept_id", deptId);
        }
        if (departmentRepository.selectCount(qw) > 0) {
            throw new BizException(400, "科室编码已存在");
        }
    }

    private DepartmentDto toDto(HospitalDepartment dept) {
        DepartmentDto dto = new DepartmentDto();
        fillDto(dto, dept);
        return dto;
    }

    private void fillDto(DepartmentDto dto, HospitalDepartment dept) {
        dto.setDeptId(dept.getDeptId());
        dto.setDeptName(dept.getDeptName());
        dto.setDeptCode(dept.getDeptCode());
        dto.setParentId(dept.getParentId());
        dto.setDescription(dept.getDescription());
        dto.setSort(dept.getSort());
        dto.setStatus(dept.getStatus());
        dto.setCreateTime(dept.getCreateTime());
        dto.setUpdateTime(dept.getUpdateTime());
    }
}
