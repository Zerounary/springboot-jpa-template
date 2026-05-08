package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.MedicalRecordCreateRequest;
import com.app.backend.dto.MedicalRecordDto;
import com.app.backend.dto.MedicalRecordUpdateRequest;
import com.app.backend.entity.DoctorInfo;
import com.app.backend.entity.MedicalRecord;
import com.app.backend.entity.PatientInfo;
import com.app.backend.entity.RegistrationRecord;
import com.app.backend.entity.User;
import com.app.backend.repository.DoctorInfoRepository;
import com.app.backend.repository.MedicalRecordRepository;
import com.app.backend.repository.PatientInfoRepository;
import com.app.backend.repository.RegistrationRecordRepository;
import com.app.backend.repository.UserRepository;
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

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final RegistrationRecordRepository registrationRecordRepository;
    private final PatientInfoRepository patientInfoRepository;
    private final DoctorInfoRepository doctorInfoRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                               RegistrationRecordRepository registrationRecordRepository,
                               PatientInfoRepository patientInfoRepository,
                               DoctorInfoRepository doctorInfoRepository,
                               UserRepository userRepository,
                               UserService userService) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.registrationRecordRepository = registrationRecordRepository;
        this.patientInfoRepository = patientInfoRepository;
        this.doctorInfoRepository = doctorInfoRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional
    public MedicalRecordDto create(Long operatorUserId, MedicalRecordCreateRequest req) {
        User operator = userService.getById(operatorUserId);
        if (operator.getRoleType() == null) {
            throw new BizException(403, "无权限");
        }
        if (operator.getRoleType() == 3) {
            throw new BizException(403, "患者不可创建病历");
        }

        MedicalRecord mr = new MedicalRecord();
        mr.setIsDeleted(0);

        if (operator.getRoleType() == 2) {
            if (req.getRegistrationId() == null) {
                throw new BizException(400, "registrationId 必填");
            }

            DoctorInfo di = getDoctorByUserId(operatorUserId);
            RegistrationRecord rr = getActiveRegistration(req.getRegistrationId());
            if (!di.getDoctorId().equals(rr.getDoctorId())) {
                throw new BizException(403, "无权限");
            }
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

            if (req.getRegistrationId() != null) {
                RegistrationRecord rr = getActiveRegistration(req.getRegistrationId());
                if (!req.getDoctorId().equals(rr.getDoctorId())
                        || !req.getPatientId().equals(rr.getPatientId())
                        || !req.getDeptId().equals(rr.getDeptId())) {
                    throw new BizException(400, "挂号与病历信息不匹配");
                }

                QueryWrapper<MedicalRecord> exists = new QueryWrapper<>();
                exists.eq("registration_id", rr.getRegistrationId());
                exists.eq("is_deleted", 0);
                if (medicalRecordRepository.selectCount(exists) > 0) {
                    throw new BizException(400, "该挂号已存在病历");
                }
            }
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
        User operator = userService.getById(operatorUserId);
        MedicalRecord mr = getActiveById(recordId);

        if (operator.getRoleType() == null) {
            throw new BizException(403, "无权限");
        }
        if (operator.getRoleType() == 3) {
            throw new BizException(403, "患者不可编辑病历");
        }

        if (operator.getRoleType() == 2) {
            DoctorInfo di = getDoctorByUserId(operatorUserId);
            if (!di.getDoctorId().equals(mr.getDoctorId())) {
                throw new BizException(403, "无权限");
            }
            if (mr.getRecordStatus() != null && mr.getRecordStatus() == 1) {
                throw new BizException(400, "已完成病历不可编辑");
            }
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
        User operator = userService.getById(operatorUserId);
        MedicalRecord mr = getActiveById(recordId);

        if (operator.getRoleType() == null) {
            throw new BizException(403, "无权限");
        }

        if (operator.getRoleType() == 3) {
            Long pid = getPatientIdByUserId(operatorUserId);
            if (!pid.equals(mr.getPatientId())) {
                throw new BizException(403, "无权限");
            }
        } else if (operator.getRoleType() == 2) {
            DoctorInfo di = getDoctorByUserId(operatorUserId);
            if (!di.getDoctorId().equals(mr.getDoctorId())) {
                throw new BizException(403, "无权限");
            }
        }

        PatientInfo pi = patientInfoRepository.selectById(mr.getPatientId());
        DoctorInfo di = doctorInfoRepository.selectById(mr.getDoctorId());

        MedicalRecordDto dto = toDto(mr, pi, di);
        enrichUsers(dto, pi, di);
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
        User operator = userService.getById(operatorUserId);
        if (operator.getRoleType() == null) {
            throw new BizException(403, "无权限");
        }

        if (operator.getRoleType() == 3) {
            patientId = getPatientIdByUserId(operatorUserId);
            doctorId = null;
        } else if (operator.getRoleType() == 2) {
            DoctorInfo di = getDoctorByUserId(operatorUserId);
            // Allow doctors to query specific patient's records, otherwise filter by current doctor
            if (patientId == null) {
                doctorId = di.getDoctorId();
            }
        }

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
            qw.and(w -> w.like("diagnosis", k).or().like("chief_complaint", k));
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

        List<Long> userIds = new ArrayList<>();
        for (PatientInfo pi : patientMap.values()) {
            if (pi != null) {
                userIds.add(pi.getUserId());
            }
        }
        for (DoctorInfo di : doctorMap.values()) {
            if (di != null) {
                userIds.add(di.getUserId());
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

        List<MedicalRecordDto> outRecords = new ArrayList<>();
        for (MedicalRecord mr : records) {
            PatientInfo pi = patientMap.get(mr.getPatientId());
            DoctorInfo di = doctorMap.get(mr.getDoctorId());
            MedicalRecordDto dto = toDto(mr, pi, di);
            if (pi != null) {
                User pu = userMap.get(pi.getUserId());
                if (pu != null) {
                    dto.setPatientUserId(pu.getId());
                    dto.setPatientRealName(pu.getRealName());
                    dto.setPatientPhone(pu.getPhone());
                }
            }
            if (di != null) {
                User du = userMap.get(di.getUserId());
                if (du != null) {
                    dto.setDoctorUserId(du.getId());
                    dto.setDoctorRealName(du.getRealName());
                    dto.setDoctorPhone(du.getPhone());
                }
            }
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

    private DoctorInfo getDoctorByUserId(Long userId) {
        QueryWrapper<DoctorInfo> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        qw.eq("is_deleted", 0);
        DoctorInfo di = doctorInfoRepository.selectOne(qw);
        if (di == null) {
            throw new BizException(400, "医生档案不存在");
        }
        return di;
    }

    private void validateForComplete(MedicalRecord mr) {
        if (mr.getChiefComplaint() == null || mr.getChiefComplaint().trim().isEmpty()) {
            throw new BizException(400, "主诉必填");
        }
        if (mr.getDiagnosis() == null || mr.getDiagnosis().trim().isEmpty()) {
            throw new BizException(400, "诊断必填");
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
            dto.setPatientUserId(pi.getUserId());
        }
        if (di != null) {
            dto.setDoctorUserId(di.getUserId());
        }

        return dto;
    }

    private void enrichUsers(MedicalRecordDto dto, PatientInfo pi, DoctorInfo di) {
        List<Long> userIds = new ArrayList<>();
        if (pi != null) {
            userIds.add(pi.getUserId());
        }
        if (di != null) {
            userIds.add(di.getUserId());
        }
        if (userIds.isEmpty()) {
            return;
        }

        Map<Long, User> userMap = new HashMap<>();
        List<User> users = userRepository.selectBatchIds(userIds);
        if (users != null) {
            for (User u : users) {
                userMap.put(u.getId(), u);
            }
        }

        if (pi != null) {
            User pu = userMap.get(pi.getUserId());
            if (pu != null) {
                dto.setPatientUserId(pu.getId());
                dto.setPatientRealName(pu.getRealName());
                dto.setPatientPhone(pu.getPhone());
            }
        }
        if (di != null) {
            User du = userMap.get(di.getUserId());
            if (du != null) {
                dto.setDoctorUserId(du.getId());
                dto.setDoctorRealName(du.getRealName());
                dto.setDoctorPhone(du.getPhone());
            }
        }
    }
}
