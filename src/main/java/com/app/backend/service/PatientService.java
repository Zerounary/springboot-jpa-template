package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.PatientCreateRequest;
import com.app.backend.dto.PatientDto;
import com.app.backend.dto.PatientUpdateRequest;
import com.app.backend.entity.PatientInfo;
import com.app.backend.entity.User;
import com.app.backend.repository.PatientInfoRepository;
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
public class PatientService {

    private final PatientInfoRepository patientInfoRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordService passwordService;

    public PatientService(PatientInfoRepository patientInfoRepository, UserRepository userRepository, UserService userService, PasswordService passwordService) {
        this.patientInfoRepository = patientInfoRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordService = passwordService;
    }

    @Transactional
    public PatientDto create(Long operatorUserId, PatientCreateRequest req) {
        checkAdmin(operatorUserId);

        QueryWrapper<PatientInfo> exists = new QueryWrapper<>();
        exists.eq("username", req.getUsername());
        exists.eq("is_deleted", 0);
        if (patientInfoRepository.selectCount(exists) > 0) {
            throw new BizException(400, "用户名已存在");
        }

        PatientInfo pi = new PatientInfo();
        pi.setUsername(req.getUsername());
        pi.setPasswordHash(passwordService.hash(req.getPassword()));
        pi.setRealName(req.getRealName());
        fillUpdate(pi, req);
        pi.setIsDeleted(0);
        patientInfoRepository.insert(pi);

        return toDto(pi);
    }

    @Transactional
    public PatientDto update(Long operatorUserId, Long patientId, PatientUpdateRequest req) {
        checkAdmin(operatorUserId);
        PatientInfo pi = patientInfoRepository.selectById(patientId);
        if (pi == null || (pi.getIsDeleted() != null && pi.getIsDeleted() != 0)) {
            throw new BizException(404, "患者档案不存在");
        }

        if (req.getUsername() != null) {
            QueryWrapper<PatientInfo> exists = new QueryWrapper<>();
            exists.eq("username", req.getUsername());
            exists.eq("is_deleted", 0);
            exists.ne("patient_id", patientId);
            if (patientInfoRepository.selectCount(exists) > 0) {
                throw new BizException(400, "用户名已存在");
            }
            pi.setUsername(req.getUsername());
        }
        if (req.getPassword() != null) {
            pi.setPasswordHash(passwordService.hash(req.getPassword()));
        }
        if (req.getRealName() != null) {
            pi.setRealName(req.getRealName());
        }
        fillUpdate(pi, req);
        patientInfoRepository.updateById(pi);

        return toDto(pi);
    }

    @Transactional
    public void delete(Long operatorUserId, Long patientId) {
        checkAdmin(operatorUserId);
        PatientInfo pi = patientInfoRepository.selectById(patientId);
        if (pi == null || (pi.getIsDeleted() != null && pi.getIsDeleted() != 0)) {
            throw new BizException(404, "患者档案不存在");
        }
        pi.setIsDeleted(1);
        patientInfoRepository.updateById(pi);
    }

    @Transactional(readOnly = true)
    public PatientDto detail(Long operatorUserId, Long patientId) {
        PatientInfo pi = patientInfoRepository.selectById(patientId);
        if (pi == null || (pi.getIsDeleted() != null && pi.getIsDeleted() != 0)) {
            throw new BizException(404, "患者档案不存在");
        }
        User operator = userService.getById(operatorUserId);
        if (operator.getRoleType() != null && operator.getRoleType() == 3) {
            if (!operator.getUsername().equals(pi.getUsername())) {
                throw new BizException(403, "无权限");
            }
        }
        return toDto(pi);
    }

    @Transactional(readOnly = true)
    public IPage<PatientDto> page(Long operatorUserId, int page, int size, String keyword) {
        User operator = userService.getById(operatorUserId);
        if (operator.getRoleType() != null && operator.getRoleType() == 3) {
            QueryWrapper<PatientInfo> qw = new QueryWrapper<>();
            qw.eq("username", operator.getUsername());
            qw.eq("is_deleted", 0);
            PatientInfo pi = patientInfoRepository.selectOne(qw);
            List<PatientDto> records = new ArrayList<>();
            if (pi != null) {
                records.add(toDto(pi));
            }
            Page<PatientDto> out = new Page<>(1, 10, records.size());
            out.setRecords(records);
            return out;
        }

        Page<PatientInfo> p = new Page<>(Math.max(page, 0) + 1L, Math.min(Math.max(size, 1), 200));
        QueryWrapper<PatientInfo> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        if (keyword != null) {
            qw.like("username", keyword);
        }
        qw.orderByDesc("patient_id");
        IPage<PatientInfo> piPage = patientInfoRepository.selectPage(p, qw);
        return piPage.convert(pi -> toDto(pi));
    }

    @Transactional(readOnly = true)
    public PatientDto me(Long operatorUserId) {
        User operator = userService.getById(operatorUserId);
        if (operator.getRoleType() == null || operator.getRoleType() != 3) {
            throw new BizException(403, "仅患者可访问");
        }
        QueryWrapper<PatientInfo> qw = new QueryWrapper<>();
        qw.eq("user_id", operatorUserId);
        qw.eq("is_deleted", 0);
        PatientInfo pi = patientInfoRepository.selectOne(qw);
        if (pi == null) {
            throw new BizException(404, "患者档案不存在");
        }
        return toDto(pi);
    }

    @Transactional
    public PatientDto upsertMe(Long operatorUserId, PatientUpdateRequest req) {
        User operator = userService.getById(operatorUserId);
        if (operator.getRoleType() == null || operator.getRoleType() != 3) {
            throw new BizException(403, "仅患者可访问");
        }
        QueryWrapper<PatientInfo> qw = new QueryWrapper<>();
        qw.eq("user_id", operatorUserId);
        qw.eq("is_deleted", 0);
        PatientInfo pi = patientInfoRepository.selectOne(qw);
        if (pi == null) {
            pi = new PatientInfo();
            pi.setUserId(operatorUserId);
            pi.setIsDeleted(0);
            fillUpdate(pi, req);
            patientInfoRepository.insert(pi);
        } else {
            fillUpdate(pi, req);
            patientInfoRepository.updateById(pi);
        }
        return toDto(pi);
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

    private void fillUpdate(PatientInfo pi, PatientUpdateRequest req) {
        if (req.getBirthDate() != null) {
            pi.setBirthDate(req.getBirthDate());
        }
        if (req.getAge() != null) {
            pi.setAge(req.getAge());
        }
        if (req.getBloodType() != null) {
            pi.setBloodType(req.getBloodType());
        }
        if (req.getMaritalStatus() != null) {
            pi.setMaritalStatus(req.getMaritalStatus());
        }
        if (req.getAddress() != null) {
            pi.setAddress(req.getAddress());
        }
        if (req.getEmergencyContact() != null) {
            pi.setEmergencyContact(req.getEmergencyContact());
        }
        if (req.getEmergencyPhone() != null) {
            pi.setEmergencyPhone(req.getEmergencyPhone());
        }
        if (req.getAllergyHistory() != null) {
            pi.setAllergyHistory(req.getAllergyHistory());
        }
        if (req.getPastMedicalHistory() != null) {
            pi.setPastMedicalHistory(req.getPastMedicalHistory());
        }
    }

    private void fillUpdate(PatientInfo pi, PatientCreateRequest req) {
        if (req.getBirthDate() != null) {
            pi.setBirthDate(req.getBirthDate());
        }
        if (req.getAge() != null) {
            pi.setAge(req.getAge());
        }
        if (req.getBloodType() != null) {
            pi.setBloodType(req.getBloodType());
        }
        if (req.getMaritalStatus() != null) {
            pi.setMaritalStatus(req.getMaritalStatus());
        }
        if (req.getAddress() != null) {
            pi.setAddress(req.getAddress());
        }
        if (req.getEmergencyContact() != null) {
            pi.setEmergencyContact(req.getEmergencyContact());
        }
        if (req.getEmergencyPhone() != null) {
            pi.setEmergencyPhone(req.getEmergencyPhone());
        }
        if (req.getAllergyHistory() != null) {
            pi.setAllergyHistory(req.getAllergyHistory());
        }
        if (req.getPastMedicalHistory() != null) {
            pi.setPastMedicalHistory(req.getPastMedicalHistory());
        }
    }

    private PatientDto toDto(PatientInfo pi) {
        PatientDto dto = new PatientDto();
        dto.setPatientId(pi.getPatientId());
        dto.setUsername(pi.getUsername());
        dto.setRealName(pi.getRealName());
        dto.setBirthDate(pi.getBirthDate());
        dto.setAge(pi.getAge());
        dto.setBloodType(pi.getBloodType());
        dto.setMaritalStatus(pi.getMaritalStatus());
        dto.setAddress(pi.getAddress());
        dto.setEmergencyContact(pi.getEmergencyContact());
        dto.setEmergencyPhone(pi.getEmergencyPhone());
        dto.setAllergyHistory(pi.getAllergyHistory());
        dto.setPastMedicalHistory(pi.getPastMedicalHistory());
        dto.setCreateTime(pi.getCreateTime());
        dto.setUpdateTime(pi.getUpdateTime());
        return dto;
    }
}
