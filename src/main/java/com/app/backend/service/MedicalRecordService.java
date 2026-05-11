package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.MedicalRecordCreateRequest;
import com.app.backend.dto.MedicalRecordDto;
import com.app.backend.dto.MedicalRecordUpdateRequest;
import com.app.backend.entity.DoctorInfo;
import com.app.backend.entity.HealthMonitor;
import com.app.backend.entity.HospitalDepartment;
import com.app.backend.entity.MedicalRecord;
import com.app.backend.entity.PatientInfo;
import com.app.backend.entity.RegistrationRecord;
import com.app.backend.entity.User;
import com.app.backend.repository.DoctorInfoRepository;
import com.app.backend.repository.HealthMonitorRepository;
import com.app.backend.repository.HospitalDepartmentRepository;
import com.app.backend.repository.MedicalRecordRepository;
import com.app.backend.repository.PatientInfoRepository;
import com.app.backend.repository.RegistrationRecordRepository;
import com.app.backend.repository.UserRepository;
import com.app.backend.util.HealthRiskAssessment;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final RegistrationRecordRepository registrationRecordRepository;
    private final PatientInfoRepository patientInfoRepository;
    private final DoctorInfoRepository doctorInfoRepository;
    private final HospitalDepartmentRepository departmentRepository;
    private final HealthMonitorRepository healthMonitorRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                               RegistrationRecordRepository registrationRecordRepository,
                               PatientInfoRepository patientInfoRepository,
                               DoctorInfoRepository doctorInfoRepository,
                               HospitalDepartmentRepository departmentRepository,
                               HealthMonitorRepository healthMonitorRepository,
                               UserRepository userRepository,
                               UserService userService) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.registrationRecordRepository = registrationRecordRepository;
        this.patientInfoRepository = patientInfoRepository;
        this.doctorInfoRepository = doctorInfoRepository;
        this.departmentRepository = departmentRepository;
        this.healthMonitorRepository = healthMonitorRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional
    public MedicalRecordDto create(Long operatorUserId, MedicalRecordCreateRequest req) {
        // 暂时移除权限检查
        MedicalRecord mr = new MedicalRecord();
        mr.setIsDeleted(0);

        if (req.getRegistrationId() != null) {
            RegistrationRecord rr = getActiveRegistration(req.getRegistrationId());
            if (rr.getRegistrationStatus() != null && rr.getRegistrationStatus() == 2) {
                throw new BizException(400, "挂号已取消");
            }

            QueryWrapper<MedicalRecord> exists = new QueryWrapper<>();
            exists.eq("registration_id", rr.getRegistrationId());
            exists.eq("is_deleted", 0);
            if (medicalRecordRepository.selectCount(exists) > 0) {
                throw new BizException(400, "该挂号已存在病历");
            }

            mr.setRegistrationId(rr.getRegistrationId());
            mr.setPatientId(rr.getPatientId());
            mr.setDoctorId(rr.getDoctorId());
            mr.setDeptId(rr.getDeptId());
        } else {
            if (req.getPatientId() == null || req.getDoctorId() == null || req.getDeptId() == null) {
                throw new BizException(400, "patientId/doctorId/deptId 必填");
            }
            mr.setPatientId(req.getPatientId());
            mr.setDoctorId(req.getDoctorId());
            mr.setDeptId(req.getDeptId());
            mr.setRegistrationId(req.getRegistrationId());
        }

        if (req.getVisitDate() != null) {
            mr.setVisitDate(req.getVisitDate());
        } else {
            mr.setVisitDate(LocalDateTime.now());
        }

        mr.setChiefComplaint(req.getChiefComplaint() != null ? req.getChiefComplaint() : "");
        mr.setPresentIllness(req.getPresentIllness());
        mr.setPastHistory(req.getPastHistory());
        mr.setPhysicalExamination(req.getPhysicalExamination());
        mr.setAuxiliaryExamination(req.getAuxiliaryExamination());
        mr.setDiagnosis(req.getDiagnosis() != null ? req.getDiagnosis() : "");
        mr.setTreatmentPlan(req.getTreatmentPlan());
        mr.setRecordStatus(req.getRecordStatus() != null ? req.getRecordStatus() : 0);

        if (mr.getRecordStatus() != null && mr.getRecordStatus() == 1) {
            validateForComplete(mr);
        }

        medicalRecordRepository.insert(mr);

        return detail(operatorUserId, mr.getRecordId());
    }

    @Transactional
    public MedicalRecordDto update(Long operatorUserId, Long recordId, MedicalRecordUpdateRequest req) {
        // 暂时移除权限检查
        MedicalRecord mr = getActiveById(recordId);

        if (mr.getRecordStatus() != null && mr.getRecordStatus() == 1) {
            throw new BizException(400, "已完成病历不可编辑");
        }

        if (req.getVisitDate() != null) {
            mr.setVisitDate(req.getVisitDate());
        }
        if (req.getChiefComplaint() != null) {
            mr.setChiefComplaint(req.getChiefComplaint());
        }
        if (req.getPresentIllness() != null) {
            mr.setPresentIllness(req.getPresentIllness());
        }
        if (req.getPastHistory() != null) {
            mr.setPastHistory(req.getPastHistory());
        }
        if (req.getPhysicalExamination() != null) {
            mr.setPhysicalExamination(req.getPhysicalExamination());
        }
        if (req.getAuxiliaryExamination() != null) {
            mr.setAuxiliaryExamination(req.getAuxiliaryExamination());
        }
        if (req.getDiagnosis() != null) {
            mr.setDiagnosis(req.getDiagnosis());
        }
        if (req.getTreatmentPlan() != null) {
            mr.setTreatmentPlan(req.getTreatmentPlan());
        }

        if (req.getRecordStatus() != null) {
            if (req.getRecordStatus() == 1) {
                validateForComplete(mr);
                mr.setRecordStatus(1);
            } else {
                mr.setRecordStatus(req.getRecordStatus());
            }
        }

        medicalRecordRepository.updateById(mr);
        return detail(operatorUserId, recordId);
    }

    @Transactional
    public MedicalRecordDto complete(Long operatorUserId, Long recordId) {
        MedicalRecordUpdateRequest req = new MedicalRecordUpdateRequest();
        req.setRecordStatus(1);
        return update(operatorUserId, recordId, req);
    }

    @Transactional
    public void delete(Long operatorUserId, Long recordId) {
        User operator = userService.getById(operatorUserId);
        if (operator.getRoleType() == null || operator.getRoleType() != 1) {
            throw new BizException(403, "无权限");
        }
        MedicalRecord mr = getActiveById(recordId);
        mr.setIsDeleted(1);
        medicalRecordRepository.updateById(mr);
    }

    @Transactional(readOnly = true)
    public MedicalRecordDto detail(Long operatorUserId, Long recordId) {
        // 暂时移除权限检查
        MedicalRecord mr = getActiveById(recordId);

        PatientInfo pi = patientInfoRepository.selectById(mr.getPatientId());
        DoctorInfo di = doctorInfoRepository.selectById(mr.getDoctorId());

        MedicalRecordDto dto = toDto(mr, pi, di);
        return dto;
    }

    @Transactional(readOnly = true)
    public IPage<MedicalRecordDto> page(Long operatorUserId,
                                       int page,
                                       int size,
                                       Long patientId,
                                       Long doctorId,
                                       Long deptId,
                                       Long registrationId,
                                       Integer recordStatus,
                                       String keyword,
                                       LocalDateTime visitFrom,
                                       LocalDateTime visitTo) {
        // 暂时移除权限检查

        Page<MedicalRecord> p = new Page<>(Math.max(page, 0) + 1L, Math.min(Math.max(size, 1), 200));
        QueryWrapper<MedicalRecord> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);

        if (patientId != null) {
            qw.eq("patient_id", patientId);
        }
        if (doctorId != null) {
            qw.eq("doctor_id", doctorId);
        }
        if (deptId != null) {
            qw.eq("dept_id", deptId);
        }
        if (registrationId != null) {
            qw.eq("registration_id", registrationId);
        }
        if (recordStatus != null) {
            qw.eq("record_status", recordStatus);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim();
            // 搜索匹配的患者ID
            QueryWrapper<PatientInfo> patientQw = new QueryWrapper<>();
            patientQw.like("real_name", k);
            patientQw.eq("is_deleted", 0);
            patientQw.select("patient_id");
            List<PatientInfo> matchingPatients = patientInfoRepository.selectList(patientQw);
            List<Long> patientIds = matchingPatients.stream().map(PatientInfo::getPatientId).collect(Collectors.toList());

            // 搜索匹配的医生ID
            QueryWrapper<DoctorInfo> doctorQw = new QueryWrapper<>();
            doctorQw.like("real_name", k);
            doctorQw.eq("is_deleted", 0);
            doctorQw.select("doctor_id");
            List<DoctorInfo> matchingDoctors = doctorInfoRepository.selectList(doctorQw);
            List<Long> doctorIds = matchingDoctors.stream().map(DoctorInfo::getDoctorId).collect(Collectors.toList());

            qw.and(w -> {
                w.like("diagnosis", k)
                 .or().like("chief_complaint", k);
                if (!patientIds.isEmpty()) {
                    w.or().in("patient_id", patientIds);
                }
                if (!doctorIds.isEmpty()) {
                    w.or().in("doctor_id", doctorIds);
                }
            });
        }
        if (visitFrom != null) {
            qw.ge("visit_date", visitFrom);
        }
        if (visitTo != null) {
            qw.le("visit_date", visitTo);
        }

        qw.orderByDesc("record_id");
        IPage<MedicalRecord> mrPage = medicalRecordRepository.selectPage(p, qw);

        List<MedicalRecord> records = mrPage.getRecords();
        if (records == null || records.isEmpty()) {
            return mrPage.convert(mr -> toDto(mr, null, null));
        }

        List<Long> pids = new ArrayList<>();
        List<Long> dids = new ArrayList<>();
        for (MedicalRecord mr : records) {
            pids.add(mr.getPatientId());
            dids.add(mr.getDoctorId());
        }

        Map<Long, PatientInfo> patientMap = new HashMap<>();
        List<PatientInfo> patients = patientInfoRepository.selectBatchIds(pids);
        if (patients != null) {
            for (PatientInfo pi : patients) {
                patientMap.put(pi.getPatientId(), pi);
            }
        }

        Map<Long, DoctorInfo> doctorMap = new HashMap<>();
        List<DoctorInfo> doctors = doctorInfoRepository.selectBatchIds(dids);
        if (doctors != null) {
            for (DoctorInfo di : doctors) {
                doctorMap.put(di.getDoctorId(), di);
            }
        }

        List<MedicalRecordDto> outRecords = new ArrayList<>();
        for (MedicalRecord mr : records) {
            PatientInfo pi = patientMap.get(mr.getPatientId());
            DoctorInfo di = doctorMap.get(mr.getDoctorId());
            MedicalRecordDto dto = toDto(mr, pi, di);
            outRecords.add(dto);
        }

        Page<MedicalRecordDto> out = new Page<>(mrPage.getCurrent(), mrPage.getSize(), mrPage.getTotal());
        out.setRecords(outRecords);
        return out;
    }

    private MedicalRecord getActiveById(Long recordId) {
        MedicalRecord mr = medicalRecordRepository.selectById(recordId);
        if (mr == null || (mr.getIsDeleted() != null && mr.getIsDeleted() != 0)) {
            throw new BizException(404, "病历不存在");
        }
        return mr;
    }

    private RegistrationRecord getActiveRegistration(Long registrationId) {
        RegistrationRecord rr = registrationRecordRepository.selectById(registrationId);
        if (rr == null || (rr.getIsDeleted() != null && rr.getIsDeleted() != 0)) {
            throw new BizException(404, "挂号记录不存在");
        }
        return rr;
    }

    private void validateForComplete(MedicalRecord mr) {
        if (mr.getDiagnosis() == null || mr.getDiagnosis().trim().isEmpty()) {
            throw new BizException(400, "诊断不能为空");
        }
    }

    private MedicalRecordDto toDto(MedicalRecord mr, PatientInfo pi, DoctorInfo di) {
        MedicalRecordDto dto = new MedicalRecordDto();
        dto.setRecordId(mr.getRecordId());
        dto.setPatientId(mr.getPatientId());
        dto.setDoctorId(mr.getDoctorId());
        dto.setDeptId(mr.getDeptId());
        dto.setRegistrationId(mr.getRegistrationId());
        dto.setVisitDate(mr.getVisitDate());
        dto.setChiefComplaint(mr.getChiefComplaint());
        dto.setPresentIllness(mr.getPresentIllness());
        dto.setPastHistory(mr.getPastHistory());
        dto.setPhysicalExamination(mr.getPhysicalExamination());
        dto.setAuxiliaryExamination(mr.getAuxiliaryExamination());
        dto.setDiagnosis(mr.getDiagnosis());
        dto.setTreatmentPlan(mr.getTreatmentPlan());
        dto.setRecordStatus(mr.getRecordStatus());
        dto.setCreateTime(mr.getCreateTime());
        dto.setUpdateTime(mr.getUpdateTime());

        if (pi != null) {
            dto.setPatientRealName(pi.getRealName());
        }
        if (di != null) {
            dto.setDoctorRealName(di.getRealName());
        }
        HospitalDepartment dept = departmentRepository.selectById(mr.getDeptId());
        if (dept != null) {
            dto.setDeptName(dept.getDeptName());
        }

        // 计算风险等级 - 基于患者最新的健康监测数据
        QueryWrapper<HealthMonitor> healthQuery = new QueryWrapper<>();
        healthQuery.eq("patient_id", mr.getPatientId());
        healthQuery.eq("is_deleted", 0);
        healthQuery.orderByDesc("monitor_date");
        healthQuery.last("LIMIT 1");
        HealthMonitor latestHealth = healthMonitorRepository.selectOne(healthQuery);

        if (latestHealth != null) {
            Integer systolic = latestHealth.getSystolicPressure();
            Integer diastolic = latestHealth.getDiastolicPressure();
            Double glucose = latestHealth.getBloodGlucose() != null ? latestHealth.getBloodGlucose().doubleValue() : null;
            Integer heartRate = latestHealth.getHeartRate();
            Double temperature = latestHealth.getBodyTemperature() != null ? latestHealth.getBodyTemperature().doubleValue() : null;

            String riskLevel = HealthRiskAssessment.assessRiskDisplayName(
                systolic, diastolic, glucose, heartRate, temperature
            );
            dto.setRiskLevel(riskLevel);
        } else {
            dto.setRiskLevel("无数据");
        }

        return dto;
    }
}
