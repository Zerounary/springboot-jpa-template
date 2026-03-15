package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.HealthMonitorCreateRequest;
import com.app.backend.dto.HealthMonitorDto;
import com.app.backend.dto.HealthMonitorUpdateRequest;
import com.app.backend.entity.HealthMonitor;
import com.app.backend.entity.PatientInfo;
import com.app.backend.entity.User;
import com.app.backend.repository.HealthMonitorRepository;
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
public class HealthMonitorService {

    private final HealthMonitorRepository healthMonitorRepository;
    private final PatientInfoRepository patientInfoRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public HealthMonitorService(HealthMonitorRepository healthMonitorRepository,
                               PatientInfoRepository patientInfoRepository,
                               UserRepository userRepository,
                               UserService userService) {
        this.healthMonitorRepository = healthMonitorRepository;
        this.patientInfoRepository = patientInfoRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional
    public HealthMonitorDto create(Long operatorUserId, HealthMonitorCreateRequest req) {
        User operator = userService.getById(operatorUserId);
        if (operator.getRoleType() == null) {
            throw new BizException(403, "无权限");
        }
        if (operator.getRoleType() == 2) {
            throw new BizException(403, "医生不可录入健康监测");
        }

        Long patientId;
        if (operator.getRoleType() == 1) {
            if (req.getPatientId() == null) {
                throw new BizException(400, "patientId 必填");
            }
            patientId = req.getPatientId();
        } else {
            patientId = getPatientIdByUserId(operatorUserId);
        }

        PatientInfo patient = getActivePatient(patientId);

        HealthMonitor hm = new HealthMonitor();
        hm.setPatientId(patientId);
        hm.setMonitorDate(req.getMonitorDate());
        hm.setSystolicPressure(req.getSystolicPressure());
        hm.setDiastolicPressure(req.getDiastolicPressure());
        hm.setBloodGlucose(req.getBloodGlucose());
        hm.setHeartRate(req.getHeartRate());
        hm.setBodyTemperature(req.getBodyTemperature());
        hm.setWeight(req.getWeight());
        hm.setRemark(req.getRemark());
        hm.setIsDeleted(0);
        healthMonitorRepository.insert(hm);

        return toDto(hm, patient);
    }

    @Transactional
    public HealthMonitorDto update(Long operatorUserId, Long monitorId, HealthMonitorUpdateRequest req) {
        User operator = userService.getById(operatorUserId);
        HealthMonitor hm = getActiveById(monitorId);

        if (operator.getRoleType() == null) {
            throw new BizException(403, "无权限");
        }
        if (operator.getRoleType() == 2) {
            throw new BizException(403, "医生不可修改健康监测");
        }

        if (operator.getRoleType() == 3) {
            Long patientId = getPatientIdByUserId(operatorUserId);
            if (!patientId.equals(hm.getPatientId())) {
                throw new BizException(403, "无权限");
            }
        }

        if (req.getMonitorDate() != null) {
            hm.setMonitorDate(req.getMonitorDate());
        }
        if (req.getSystolicPressure() != null) {
            hm.setSystolicPressure(req.getSystolicPressure());
        }
        if (req.getDiastolicPressure() != null) {
            hm.setDiastolicPressure(req.getDiastolicPressure());
        }
        if (req.getBloodGlucose() != null) {
            hm.setBloodGlucose(req.getBloodGlucose());
        }
        if (req.getHeartRate() != null) {
            hm.setHeartRate(req.getHeartRate());
        }
        if (req.getBodyTemperature() != null) {
            hm.setBodyTemperature(req.getBodyTemperature());
        }
        if (req.getWeight() != null) {
            hm.setWeight(req.getWeight());
        }
        if (req.getRemark() != null) {
            hm.setRemark(req.getRemark());
        }

        healthMonitorRepository.updateById(hm);

        PatientInfo patient = patientInfoRepository.selectById(hm.getPatientId());
        return toDto(hm, patient);
    }

    @Transactional
    public void delete(Long operatorUserId, Long monitorId) {
        User operator = userService.getById(operatorUserId);
        HealthMonitor hm = getActiveById(monitorId);

        if (operator.getRoleType() == null) {
            throw new BizException(403, "无权限");
        }
        if (operator.getRoleType() == 2) {
            throw new BizException(403, "医生不可删除健康监测");
        }

        if (operator.getRoleType() == 3) {
            Long patientId = getPatientIdByUserId(operatorUserId);
            if (!patientId.equals(hm.getPatientId())) {
                throw new BizException(403, "无权限");
            }
        }

        hm.setIsDeleted(1);
        healthMonitorRepository.updateById(hm);
    }

    @Transactional(readOnly = true)
    public HealthMonitorDto detail(Long operatorUserId, Long monitorId) {
        User operator = userService.getById(operatorUserId);
        HealthMonitor hm = getActiveById(monitorId);

        if (operator.getRoleType() == null) {
            throw new BizException(403, "无权限");
        }

        if (operator.getRoleType() == 3) {
            Long patientId = getPatientIdByUserId(operatorUserId);
            if (!patientId.equals(hm.getPatientId())) {
                throw new BizException(403, "无权限");
            }
        }

        PatientInfo patient = patientInfoRepository.selectById(hm.getPatientId());
        return toDto(hm, patient);
    }

    @Transactional(readOnly = true)
    public IPage<HealthMonitorDto> page(Long operatorUserId,
                                       int page,
                                       int size,
                                       Long patientId,
                                       java.time.LocalDateTime dateFrom,
                                       java.time.LocalDateTime dateTo) {
        User operator = userService.getById(operatorUserId);
        if (operator.getRoleType() == null) {
            throw new BizException(403, "无权限");
        }

        if (operator.getRoleType() == 3) {
            patientId = getPatientIdByUserId(operatorUserId);
        }

        Page<HealthMonitor> p = new Page<>(Math.max(page, 0) + 1L, Math.min(Math.max(size, 1), 200));
        QueryWrapper<HealthMonitor> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        if (patientId != null) {
            qw.eq("patient_id", patientId);
        }
        if (dateFrom != null) {
            qw.ge("monitor_date", dateFrom);
        }
        if (dateTo != null) {
            qw.le("monitor_date", dateTo);
        }
        qw.orderByDesc("monitor_date").orderByDesc("monitor_id");

        IPage<HealthMonitor> hmPage = healthMonitorRepository.selectPage(p, qw);
        List<HealthMonitor> records = hmPage.getRecords();
        if (records == null || records.isEmpty()) {
            return hmPage.convert(hm -> toDto(hm, null));
        }

        List<Long> patientIds = new ArrayList<>();
        for (HealthMonitor hm : records) {
            patientIds.add(hm.getPatientId());
        }

        Map<Long, PatientInfo> patientMap = new HashMap<>();
        List<PatientInfo> patients = patientInfoRepository.selectBatchIds(patientIds);
        if (patients != null) {
            for (PatientInfo pi : patients) {
                patientMap.put(pi.getPatientId(), pi);
            }
        }

        List<Long> userIds = new ArrayList<>();
        for (PatientInfo pi : patientMap.values()) {
            if (pi != null) {
                userIds.add(pi.getUserId());
            }
        }

        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userRepository.selectBatchIds(userIds);
            if (users != null) {
                for (User u : users) {
                    userMap.put(u.getId(), u);
                }
            }
        }

        List<HealthMonitorDto> outRecords = new ArrayList<>();
        for (HealthMonitor hm : records) {
            PatientInfo pi = patientMap.get(hm.getPatientId());
            HealthMonitorDto dto = toDto(hm, pi);
            if (pi != null) {
                User pu = userMap.get(pi.getUserId());
                if (pu != null) {
                    dto.setPatientUserId(pu.getId());
                    dto.setPatientRealName(pu.getRealName());
                    dto.setPatientPhone(pu.getPhone());
                }
            }
            outRecords.add(dto);
        }

        Page<HealthMonitorDto> out = new Page<>(hmPage.getCurrent(), hmPage.getSize(), hmPage.getTotal());
        out.setRecords(outRecords);
        return out;
    }

    private HealthMonitor getActiveById(Long monitorId) {
        HealthMonitor hm = healthMonitorRepository.selectById(monitorId);
        if (hm == null || (hm.getIsDeleted() != null && hm.getIsDeleted() != 0)) {
            throw new BizException(404, "健康监测记录不存在");
        }
        return hm;
    }

    private Long getPatientIdByUserId(Long userId) {
        QueryWrapper<PatientInfo> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        qw.eq("is_deleted", 0);
        PatientInfo pi = patientInfoRepository.selectOne(qw);
        if (pi == null) {
            throw new BizException(400, "患者档案不存在");
        }
        return pi.getPatientId();
    }

    private PatientInfo getActivePatient(Long patientId) {
        PatientInfo pi = patientInfoRepository.selectById(patientId);
        if (pi == null || (pi.getIsDeleted() != null && pi.getIsDeleted() != 0)) {
            throw new BizException(400, "患者档案不存在");
        }
        return pi;
    }

    private HealthMonitorDto toDto(HealthMonitor hm, PatientInfo pi) {
        HealthMonitorDto dto = new HealthMonitorDto();
        dto.setMonitorId(hm.getMonitorId());
        dto.setPatientId(hm.getPatientId());
        dto.setMonitorDate(hm.getMonitorDate());
        dto.setSystolicPressure(hm.getSystolicPressure());
        dto.setDiastolicPressure(hm.getDiastolicPressure());
        dto.setBloodGlucose(hm.getBloodGlucose());
        dto.setHeartRate(hm.getHeartRate());
        dto.setBodyTemperature(hm.getBodyTemperature());
        dto.setWeight(hm.getWeight());
        dto.setRemark(hm.getRemark());
        dto.setCreateTime(hm.getCreateTime());
        dto.setUpdateTime(hm.getUpdateTime());

        if (pi != null) {
            dto.setPatientUserId(pi.getUserId());
            User pu = userRepository.selectById(pi.getUserId());
            if (pu != null) {
                dto.setPatientRealName(pu.getRealName());
                dto.setPatientPhone(pu.getPhone());
            }
        }

        return dto;
    }
}
