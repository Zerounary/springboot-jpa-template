package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.DoctorCreateRequest;
import com.app.backend.dto.DoctorDto;
import com.app.backend.dto.DoctorUpdateRequest;
import com.app.backend.entity.DoctorInfo;
import com.app.backend.entity.HospitalDepartment;
import com.app.backend.entity.RegistrationRecord;
import com.app.backend.entity.User;
import com.app.backend.repository.DoctorInfoRepository;
import com.app.backend.repository.HospitalDepartmentRepository;
import com.app.backend.repository.RegistrationRecordRepository;
import com.app.backend.repository.UserRepository;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DoctorService {

    private final DoctorInfoRepository doctorInfoRepository;
    private final UserRepository userRepository;
    private final HospitalDepartmentRepository departmentRepository;
    private final RegistrationRecordRepository registrationRecordRepository;
    private final UserService userService;
    private final PasswordService passwordService;

    public DoctorService(DoctorInfoRepository doctorInfoRepository,
                         UserRepository userRepository,
                         HospitalDepartmentRepository departmentRepository,
                         RegistrationRecordRepository registrationRecordRepository,
                         UserService userService,
                         PasswordService passwordService) {
        this.doctorInfoRepository = doctorInfoRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.registrationRecordRepository = registrationRecordRepository;
        this.userService = userService;
        this.passwordService = passwordService;
    }

    @Transactional
    public DoctorDto create(Long operatorUserId, DoctorCreateRequest req) {
        checkAdmin(operatorUserId);

        QueryWrapper<DoctorInfo> exists = new QueryWrapper<>();
        exists.eq("username", req.getUsername());
        exists.eq("is_deleted", 0);
        if (doctorInfoRepository.selectCount(exists) > 0) {
            throw new BizException(400, "用户名已存在");
        }

        HospitalDepartment dept = departmentRepository.selectById(req.getDeptId());
        if (dept == null || (dept.getIsDeleted() != null && dept.getIsDeleted() != 0)) {
            throw new BizException(400, "科室不存在");
        }

        DoctorInfo di = new DoctorInfo();
        di.setUsername(req.getUsername());
        di.setPasswordHash(passwordService.hash(req.getPassword()));
        di.setRealName(req.getRealName());
        di.setDeptId(req.getDeptId());
        di.setJobTitle(req.getJobTitle());
        di.setSpecialty(req.getSpecialty());
        di.setIntroduction(req.getIntroduction());
        di.setRegistrationFee(req.getRegistrationFee());
        di.setDailyAppointmentLimit(normalizeDailyAppointmentLimit(req.getDailyAppointmentLimit()));
        di.setSchedule(req.getSchedule());
        di.setIsDeleted(0);
        doctorInfoRepository.insert(di);

        return toDto(di, null);
    }

    @Transactional
    public DoctorDto update(Long operatorUserId, Long doctorId, DoctorUpdateRequest req) {
        checkAdmin(operatorUserId);

        DoctorInfo di = doctorInfoRepository.selectById(doctorId);
        if (di == null || (di.getIsDeleted() != null && di.getIsDeleted() != 0)) {
            throw new BizException(404, "医生不存在");
        }

        if (req.getUsername() != null) {
            QueryWrapper<DoctorInfo> exists = new QueryWrapper<>();
            exists.eq("username", req.getUsername());
            exists.eq("is_deleted", 0);
            exists.ne("doctor_id", doctorId);
            if (doctorInfoRepository.selectCount(exists) > 0) {
                throw new BizException(400, "用户名已存在");
            }
            di.setUsername(req.getUsername());
        }
        if (req.getPassword() != null) {
            di.setPasswordHash(passwordService.hash(req.getPassword()));
        }
        if (req.getRealName() != null) {
            di.setRealName(req.getRealName());
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
        if (req.getDailyAppointmentLimit() != null) {
            di.setDailyAppointmentLimit(normalizeDailyAppointmentLimit(req.getDailyAppointmentLimit()));
        }
        if (req.getSchedule() != null) {
            di.setSchedule(req.getSchedule());
        }

        doctorInfoRepository.updateById(di);

        return toDto(di, null);
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
        return toDto(di, null);
    }

    @Transactional(readOnly = true)
    public IPage<DoctorDto> page(int page, int size, Long deptId, String keyword, LocalDate scheduleDate) {
        Page<DoctorInfo> p = new Page<>(Math.max(page, 0) + 1L, Math.min(Math.max(size, 1), 200));
        QueryWrapper<DoctorInfo> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        if (deptId != null) {
            qw.eq("dept_id", deptId);
        }
        if (keyword != null) {
            qw.like("username", keyword);
        }
        qw.orderByDesc("doctor_id");

        IPage<DoctorInfo> diPage = doctorInfoRepository.selectPage(p, qw);
        return diPage.convert(di -> toDto(di, scheduleDate));
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

    private Integer normalizeDailyAppointmentLimit(Integer dailyAppointmentLimit) {
        if (dailyAppointmentLimit == null) {
            return null;
        }
        if (dailyAppointmentLimit < 0) {
            throw new BizException(400, "每日限号数量不能小于 0");
        }
        return dailyAppointmentLimit;
    }

    private int countActiveRegistrations(Long doctorId, LocalDate scheduleDate) {
        if (scheduleDate == null) {
            return 0;
        }
        QueryWrapper<RegistrationRecord> qw = new QueryWrapper<>();
        qw.eq("doctor_id", doctorId);
        qw.eq("schedule_date", scheduleDate);
        qw.eq("is_deleted", 0);
        qw.in("registration_status", 0, 1);
        return Math.toIntExact(registrationRecordRepository.selectCount(qw));
    }

    private DoctorDto toDto(DoctorInfo di, LocalDate scheduleDate) {
        DoctorDto dto = new DoctorDto();
        dto.setDoctorId(di.getDoctorId());
        dto.setUsername(di.getUsername());
        dto.setRealName(di.getRealName());
        dto.setDeptId(di.getDeptId());
        dto.setJobTitle(di.getJobTitle());
        dto.setSpecialty(di.getSpecialty());
        dto.setIntroduction(di.getIntroduction());
        dto.setRegistrationFee(di.getRegistrationFee());
        dto.setSchedule(di.getSchedule());
        dto.setDailyAppointmentLimit(di.getDailyAppointmentLimit());
        if (di.getDailyAppointmentLimit() != null) {
            int remaining = di.getDailyAppointmentLimit() - countActiveRegistrations(di.getDoctorId(), scheduleDate);
            dto.setRemainingAppointmentCount(Math.max(remaining, 0));
        }
        dto.setCreateTime(di.getCreateTime());
        dto.setUpdateTime(di.getUpdateTime());
        return dto;
    }
}
