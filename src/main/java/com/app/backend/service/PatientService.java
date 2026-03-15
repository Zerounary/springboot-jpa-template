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

    public PatientService(PatientInfoRepository patientInfoRepository, UserRepository userRepository, UserService userService) {
        this.patientInfoRepository = patientInfoRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional
    public PatientDto create(Long operatorUserId, PatientCreateRequest req) {
        checkAdmin(operatorUserId);
        User user = userService.getById(req.getUserId());
        if (user.getRoleType() == null || user.getRoleType() != 3) {
            throw new BizException(400, "用户不是患者角色");
        }

        QueryWrapper<PatientInfo> exists = new QueryWrapper<>();
        exists.eq("user_id", req.getUserId());
        exists.eq("is_deleted", 0);
        if (patientInfoRepository.selectCount(exists) > 0) {
            throw new BizException(400, "该用户已存在患者档案");
        }

        PatientInfo pi = new PatientInfo();
        pi.setUserId(req.getUserId());
        fillUpdate(pi, req);
        pi.setIsDeleted(0);
        patientInfoRepository.insert(pi);

        return toDto(pi, user);
    }

    @Transactional
    public PatientDto update(Long operatorUserId, Long patientId, PatientUpdateRequest req) {
        checkAdmin(operatorUserId);
        PatientInfo pi = patientInfoRepository.selectById(patientId);
        if (pi == null || (pi.getIsDeleted() != null && pi.getIsDeleted() != 0)) {
            throw new BizException(404, "患者档案不存在");
        }
        fillUpdate(pi, req);
        patientInfoRepository.updateById(pi);

        User user = userRepository.selectById(pi.getUserId());
        return toDto(pi, user);
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
            if (!operator.getId().equals(pi.getUserId())) {
                throw new BizException(403, "无权限");
            }
        }
        User user = userRepository.selectById(pi.getUserId());
        return toDto(pi, user);
    }

    @Transactional(readOnly = true)
    public IPage<PatientDto> page(Long operatorUserId, int page, int size, String keyword) {
        User operator = userService.getById(operatorUserId);
        if (operator.getRoleType() != null && operator.getRoleType() == 3) {
            QueryWrapper<PatientInfo> qw = new QueryWrapper<>();
            qw.eq("user_id", operatorUserId);
            qw.eq("is_deleted", 0);
            PatientInfo pi = patientInfoRepository.selectOne(qw);
            List<PatientDto> records = new ArrayList<>();
            if (pi != null) {
                records.add(toDto(pi, operator));
            }
            Page<PatientDto> out = new Page<>(1, 10, records.size());
            out.setRecords(records);
            return out;
        }

        Page<PatientInfo> p = new Page<>(Math.max(page, 0) + 1L, Math.min(Math.max(size, 1), 200));
        QueryWrapper<PatientInfo> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        qw.orderByDesc("patient_id");
        IPage<PatientInfo> piPage = patientInfoRepository.selectPage(p, qw);

        List<PatientInfo> records = piPage.getRecords();
        if (records == null || records.isEmpty()) {
            return piPage.convert(pi -> toDto(pi, null));
        }

        List<Long> userIds = new ArrayList<>();
        for (PatientInfo pi : records) {
            userIds.add(pi.getUserId());
        }
        Map<Long, User> userMap = new HashMap<>();
        List<User> users = userRepository.selectBatchIds(userIds);
        if (users != null) {
            for (User u : users) {
                userMap.put(u.getId(), u);
            }
        }

        List<PatientDto> dtoRecords = new ArrayList<>();
        for (PatientInfo pi : records) {
            User u = userMap.get(pi.getUserId());
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
            dtoRecords.add(toDto(pi, u));
        }

        Page<PatientDto> out = new Page<>(piPage.getCurrent(), piPage.getSize(), piPage.getTotal());
        out.setRecords(dtoRecords);
        return out;
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
        return toDto(pi, operator);
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
        return toDto(pi, operator);
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

    private PatientDto toDto(PatientInfo pi, User u) {
        PatientDto dto = new PatientDto();
        dto.setPatientId(pi.getPatientId());
        dto.setUserId(pi.getUserId());
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
        if (u != null) {
            dto.setUsername(u.getUsername());
            dto.setRealName(u.getRealName());
            dto.setPhone(u.getPhone());
            dto.setGender(u.getGender());
            dto.setAvatar(u.getAvatar());
        }
        return dto;
    }
}
