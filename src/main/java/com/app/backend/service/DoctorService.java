package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.DoctorCreateRequest;
import com.app.backend.dto.DoctorDto;
import com.app.backend.dto.DoctorUpdateRequest;
import com.app.backend.entity.DoctorInfo;
import com.app.backend.entity.HospitalDepartment;
import com.app.backend.entity.User;
import com.app.backend.repository.DoctorInfoRepository;
import com.app.backend.repository.HospitalDepartmentRepository;
import com.app.backend.repository.UserRepository;
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
public class DoctorService {

    private final DoctorInfoRepository doctorInfoRepository;
    private final UserRepository userRepository;
    private final HospitalDepartmentRepository departmentRepository;
    private final UserService userService;

    public DoctorService(DoctorInfoRepository doctorInfoRepository,
                         UserRepository userRepository,
                         HospitalDepartmentRepository departmentRepository,
                         UserService userService) {
        this.doctorInfoRepository = doctorInfoRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.userService = userService;
    }

    @Transactional
    public DoctorDto create(Long operatorUserId, DoctorCreateRequest req) {
        checkAdmin(operatorUserId);

        User user = userService.getById(req.getUserId());
        if (user.getRoleType() == null || user.getRoleType() != 2) {
            throw new BizException(400, "用户不是医生角色");
        }

        QueryWrapper<DoctorInfo> exists = new QueryWrapper<>();
        exists.eq("user_id", req.getUserId());
        exists.eq("is_deleted", 0);
        if (doctorInfoRepository.selectCount(exists) > 0) {
            throw new BizException(400, "该用户已存在医生档案");
        }

        HospitalDepartment dept = departmentRepository.selectById(req.getDeptId());
        if (dept == null || (dept.getIsDeleted() != null && dept.getIsDeleted() != 0)) {
            throw new BizException(400, "科室不存在");
        }

        DoctorInfo di = new DoctorInfo();
        di.setUserId(req.getUserId());
        di.setDeptId(req.getDeptId());
        di.setJobTitle(req.getJobTitle());
        di.setSpecialty(req.getSpecialty());
        di.setIntroduction(req.getIntroduction());
        di.setRegistrationFee(req.getRegistrationFee());
        di.setSchedule(req.getSchedule());
        di.setIsDeleted(0);
        doctorInfoRepository.insert(di);

        return toDto(di, user);
    }

    @Transactional
    public DoctorDto update(Long operatorUserId, Long doctorId, DoctorUpdateRequest req) {
        checkAdmin(operatorUserId);

        DoctorInfo di = doctorInfoRepository.selectById(doctorId);
        if (di == null || (di.getIsDeleted() != null && di.getIsDeleted() != 0)) {
            throw new BizException(404, "医生不存在");
        }

        if (req.getDeptId() != null) {
            HospitalDepartment dept = departmentRepository.selectById(req.getDeptId());
            if (dept == null || (dept.getIsDeleted() != null && dept.getIsDeleted() != 0)) {
                throw new BizException(400, "科室不存在");
            }
            di.setDeptId(req.getDeptId());
        }
        if (req.getJobTitle() != null) {
            di.setJobTitle(req.getJobTitle());
        }
        if (req.getSpecialty() != null) {
            di.setSpecialty(req.getSpecialty());
        }
        if (req.getIntroduction() != null) {
            di.setIntroduction(req.getIntroduction());
        }
        if (req.getRegistrationFee() != null) {
            di.setRegistrationFee(req.getRegistrationFee());
        }
        if (req.getSchedule() != null) {
            di.setSchedule(req.getSchedule());
        }

        doctorInfoRepository.updateById(di);

        User user = userRepository.selectById(di.getUserId());
        return toDto(di, user);
    }

    @Transactional
    public void delete(Long operatorUserId, Long doctorId) {
        checkAdmin(operatorUserId);

        DoctorInfo di = doctorInfoRepository.selectById(doctorId);
        if (di == null || (di.getIsDeleted() != null && di.getIsDeleted() != 0)) {
            throw new BizException(404, "医生不存在");
        }
        di.setIsDeleted(1);
        doctorInfoRepository.updateById(di);
    }

    @Transactional(readOnly = true)
    public DoctorDto detail(Long doctorId) {
        DoctorInfo di = doctorInfoRepository.selectById(doctorId);
        if (di == null || (di.getIsDeleted() != null && di.getIsDeleted() != 0)) {
            throw new BizException(404, "医生不存在");
        }
        User user = userRepository.selectById(di.getUserId());
        return toDto(di, user);
    }

    @Transactional(readOnly = true)
    public IPage<DoctorDto> page(int page, int size, Long deptId, String keyword) {
        Page<DoctorInfo> p = new Page<>(Math.max(page, 0) + 1L, Math.min(Math.max(size, 1), 200));
        QueryWrapper<DoctorInfo> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        if (deptId != null) {
            qw.eq("dept_id", deptId);
        }
        qw.orderByDesc("doctor_id");

        IPage<DoctorInfo> diPage = doctorInfoRepository.selectPage(p, qw);
        List<DoctorInfo> records = diPage.getRecords();
        if (records == null || records.isEmpty()) {
            return diPage.convert(di -> toDto(di, null));
        }

        List<Long> userIds = new ArrayList<>();
        for (DoctorInfo di : records) {
            userIds.add(di.getUserId());
        }
        Map<Long, User> userMap = new HashMap<>();
        List<User> users = userRepository.selectBatchIds(userIds);
        if (users != null) {
            for (User u : users) {
                userMap.put(u.getId(), u);
            }
        }

        List<DoctorDto> dtoRecords = new ArrayList<>();
        for (DoctorInfo di : records) {
            User u = userMap.get(di.getUserId());
            if (keyword != null) {
                String kw = keyword;
                boolean match = false;
                if (u != null) {
                    if (u.getUsername() != null && u.getUsername().contains(kw)) {
                        match = true;
                    } else if (u.getRealName() != null && u.getRealName().contains(kw)) {
                        match = true;
                    } else if (u.getPhone() != null && u.getPhone().contains(kw)) {
                        match = true;
                    }
                }
                if (!match) {
                    continue;
                }
            }
            dtoRecords.add(toDto(di, u));
        }

        Page<DoctorDto> out = new Page<>(diPage.getCurrent(), diPage.getSize(), diPage.getTotal());
        out.setRecords(dtoRecords);
        return out;
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

    private DoctorDto toDto(DoctorInfo di, User u) {
        DoctorDto dto = new DoctorDto();
        dto.setDoctorId(di.getDoctorId());
        dto.setUserId(di.getUserId());
        dto.setDeptId(di.getDeptId());
        dto.setJobTitle(di.getJobTitle());
        dto.setSpecialty(di.getSpecialty());
        dto.setIntroduction(di.getIntroduction());
        dto.setRegistrationFee(di.getRegistrationFee());
        dto.setSchedule(di.getSchedule());
        dto.setCreateTime(di.getCreateTime());
        dto.setUpdateTime(di.getUpdateTime());
        if (u != null) {
            dto.setUsername(u.getUsername());
            dto.setRealName(u.getRealName());
            dto.setPhone(u.getPhone());
            dto.setAvatar(u.getAvatar());
        }
        return dto;
    }
}
