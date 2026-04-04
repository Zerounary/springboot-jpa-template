package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.RegistrationCreateRequest;
import com.app.backend.dto.RegistrationDto;
import com.app.backend.entity.DoctorInfo;
import com.app.backend.entity.HospitalDepartment;
import com.app.backend.entity.PatientInfo;
import com.app.backend.entity.RegistrationRecord;
import com.app.backend.entity.User;
import com.app.backend.repository.DoctorInfoRepository;
import com.app.backend.repository.HospitalDepartmentRepository;
import com.app.backend.repository.PatientInfoRepository;
import com.app.backend.repository.RegistrationRecordRepository;
import com.app.backend.repository.UserRepository;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RegistrationService {

    private final RegistrationRecordRepository registrationRecordRepository;
    private final PatientInfoRepository patientInfoRepository;
    private final DoctorInfoRepository doctorInfoRepository;
    private final HospitalDepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public RegistrationService(RegistrationRecordRepository registrationRecordRepository,
                               PatientInfoRepository patientInfoRepository,
                               DoctorInfoRepository doctorInfoRepository,
                               HospitalDepartmentRepository departmentRepository,
                               UserRepository userRepository,
                               UserService userService) {
        this.registrationRecordRepository = registrationRecordRepository;
        this.patientInfoRepository = patientInfoRepository;
        this.doctorInfoRepository = doctorInfoRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional
    public RegistrationDto create(Long operatorUserId, RegistrationCreateRequest req) {
        User operator = userService.getById(operatorUserId);
        if (operator.getRoleType() == null) {
            throw new BizException(403, "无权限");
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

        if (req.getScheduleDate() == null) {
            throw new BizException(400, "scheduleDate 必填");
        }
        if (req.getScheduleDate().isBefore(LocalDate.now())) {
            throw new BizException(400, "预约日期不能早于今天");
        }

        PatientInfo patient = patientInfoRepository.selectById(patientId);
        if (patient == null || (patient.getIsDeleted() != null && patient.getIsDeleted() != 0)) {
            throw new BizException(400, "患者档案不存在");
        }

        DoctorInfo doctor = doctorInfoRepository.selectById(req.getDoctorId());
        if (doctor == null || (doctor.getIsDeleted() != null && doctor.getIsDeleted() != 0)) {
            throw new BizException(400, "医生不存在");
        }

        if (!doctor.getDeptId().equals(req.getDeptId())) {
            throw new BizException(400, "医生与科室不匹配");
        }

        HospitalDepartment dept = departmentRepository.selectById(req.getDeptId());
        if (dept == null || (dept.getIsDeleted() != null && dept.getIsDeleted() != 0)) {
            throw new BizException(400, "科室不存在");
        }

        RegistrationRecord rr = new RegistrationRecord();
        rr.setRegistrationNo(generateRegistrationNo());
        rr.setPatientId(patientId);
        rr.setDoctorId(req.getDoctorId());
        rr.setDeptId(req.getDeptId());
        rr.setScheduleDate(req.getScheduleDate());
        rr.setTimeSlot(req.getTimeSlot());
        rr.setRegistrationFee(doctor.getRegistrationFee());
        rr.setPayStatus(0);
        rr.setRegistrationStatus(0);
        rr.setVisitSerialNumber(nextVisitSerialNumber(req.getDoctorId(), req.getScheduleDate(), req.getTimeSlot()));
        rr.setRemark(req.getRemark());
        rr.setIsDeleted(0);

        registrationRecordRepository.insert(rr);

        return toDto(rr, patient, doctor);
    }

    @Transactional
    public RegistrationDto pay(Long operatorUserId, Long registrationId) {
        User operator = userService.getById(operatorUserId);
        RegistrationRecord rr = getActiveById(registrationId);

        if (operator.getRoleType() == null) {
            throw new BizException(403, "无权限");
        }
        if (operator.getRoleType() == 3) {
            Long patientId = getPatientIdByUserId(operatorUserId);
            if (!patientId.equals(rr.getPatientId())) {
                throw new BizException(403, "无权限");
            }
        } else if (operator.getRoleType() != 1) {
            throw new BizException(403, "无权限");
        }

        if (rr.getRegistrationStatus() == null || rr.getRegistrationStatus() != 0) {
            throw new BizException(400, "当前挂号状态不允许支付");
        }
        if (rr.getPayStatus() == null || rr.getPayStatus() != 0) {
            throw new BizException(400, "当前支付状态不允许支付");
        }

        rr.setPayStatus(1);
        registrationRecordRepository.updateById(rr);

        PatientInfo patient = patientInfoRepository.selectById(rr.getPatientId());
        DoctorInfo doctor = doctorInfoRepository.selectById(rr.getDoctorId());
        return toDto(rr, patient, doctor);
    }

    @Transactional
    public RegistrationDto cancel(Long operatorUserId, Long registrationId) {
        User operator = userService.getById(operatorUserId);
        RegistrationRecord rr = getActiveById(registrationId);

        if (operator.getRoleType() == null) {
            throw new BizException(403, "无权限");
        }

        if (operator.getRoleType() == 3) {
            Long patientId = getPatientIdByUserId(operatorUserId);
            if (!patientId.equals(rr.getPatientId())) {
                throw new BizException(403, "无权限");
            }
            if (rr.getScheduleDate() == null || !rr.getScheduleDate().isAfter(LocalDate.now())) {
                throw new BizException(400, "仅允许在就诊日期之前取消");
            }
        } else if (operator.getRoleType() != 1) {
            throw new BizException(403, "无权限");
        }

        if (rr.getRegistrationStatus() != null && rr.getRegistrationStatus() == 2) {
            throw new BizException(400, "已取消");
        }
        if (rr.getRegistrationStatus() != null && rr.getRegistrationStatus() == 1) {
            throw new BizException(400, "已就诊不可取消");
        }

        rr.setRegistrationStatus(2);
        if (rr.getPayStatus() != null && rr.getPayStatus() == 1) {
            rr.setPayStatus(2);
        }
        registrationRecordRepository.updateById(rr);

        PatientInfo patient = patientInfoRepository.selectById(rr.getPatientId());
        DoctorInfo doctor = doctorInfoRepository.selectById(rr.getDoctorId());
        return toDto(rr, patient, doctor);
    }

    @Transactional
    public RegistrationDto markVisited(Long operatorUserId, Long registrationId) {
        User operator = userService.getById(operatorUserId);
        RegistrationRecord rr = getActiveById(registrationId);

        if (operator.getRoleType() == null) {
            throw new BizException(403, "无权限");
        }

        if (operator.getRoleType() == 2) {
            DoctorInfo di = getDoctorByUserId(operatorUserId);
            if (!di.getDoctorId().equals(rr.getDoctorId())) {
                throw new BizException(403, "无权限");
            }
        } else if (operator.getRoleType() != 1) {
            throw new BizException(403, "无权限");
        }

        if (rr.getRegistrationStatus() == null || rr.getRegistrationStatus() != 0) {
            throw new BizException(400, "当前挂号状态不允许变更为已就诊");
        }

        rr.setRegistrationStatus(1);
        registrationRecordRepository.updateById(rr);

        PatientInfo patient = patientInfoRepository.selectById(rr.getPatientId());
        DoctorInfo doctor = doctorInfoRepository.selectById(rr.getDoctorId());
        return toDto(rr, patient, doctor);
    }

    @Transactional(readOnly = true)
    public RegistrationDto detail(Long operatorUserId, Long registrationId) {
        User operator = userService.getById(operatorUserId);
        RegistrationRecord rr = getActiveById(registrationId);

        if (operator.getRoleType() == null) {
            throw new BizException(403, "无权限");
        }

        if (operator.getRoleType() == 3) {
            Long patientId = getPatientIdByUserId(operatorUserId);
            if (!patientId.equals(rr.getPatientId())) {
                throw new BizException(403, "无权限");
            }
        } else if (operator.getRoleType() == 2) {
            DoctorInfo di = getDoctorByUserId(operatorUserId);
            if (!di.getDoctorId().equals(rr.getDoctorId())) {
                throw new BizException(403, "无权限");
            }
        }

        PatientInfo patient = patientInfoRepository.selectById(rr.getPatientId());
        DoctorInfo doctor = doctorInfoRepository.selectById(rr.getDoctorId());
        return toDto(rr, patient, doctor);
    }

    @Transactional(readOnly = true)
    public IPage<RegistrationDto> page(Long operatorUserId,
                                      int page,
                                      int size,
                                      Integer payStatus,
                                      Integer registrationStatus,
                                      Long deptId,
                                      Long doctorId,
                                      Long patientId,
                                      LocalDate dateFrom,
                                      LocalDate dateTo) {
        User operator = userService.getById(operatorUserId);
        if (operator.getRoleType() == null) {
            throw new BizException(403, "无权限");
        }

        if (operator.getRoleType() == 3) {
            patientId = getPatientIdByUserId(operatorUserId);
            doctorId = null;
        } else if (operator.getRoleType() == 2) {
            DoctorInfo di = getDoctorByUserId(operatorUserId);
            doctorId = di.getDoctorId();
            patientId = null;
        }

        Page<RegistrationRecord> p = new Page<>(Math.max(page, 0) + 1L, Math.min(Math.max(size, 1), 200));
        QueryWrapper<RegistrationRecord> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);

        if (payStatus != null) {
            qw.eq("pay_status", payStatus);
        }
        if (registrationStatus != null) {
            qw.eq("registration_status", registrationStatus);
        }
        if (deptId != null) {
            qw.eq("dept_id", deptId);
        }
        if (doctorId != null) {
            qw.eq("doctor_id", doctorId);
        }
        if (patientId != null) {
            qw.eq("patient_id", patientId);
        }
        if (dateFrom != null) {
            qw.ge("schedule_date", dateFrom);
        }
        if (dateTo != null) {
            qw.le("schedule_date", dateTo);
        }

        qw.orderByDesc("registration_id");

        IPage<RegistrationRecord> rrPage = registrationRecordRepository.selectPage(p, qw);
        List<RegistrationRecord> records = rrPage.getRecords();
        if (records == null || records.isEmpty()) {
            return rrPage.convert(rr -> toDto(rr, null, null));
        }

        List<Long> pids = new ArrayList<>();
        List<Long> dids = new ArrayList<>();
        for (RegistrationRecord rr : records) {
            pids.add(rr.getPatientId());
            dids.add(rr.getDoctorId());
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

        List<RegistrationDto> outRecords = new ArrayList<>();
        for (RegistrationRecord rr : records) {
            PatientInfo pi = patientMap.get(rr.getPatientId());
            DoctorInfo di = doctorMap.get(rr.getDoctorId());
            RegistrationDto dto = toDto(rr, pi, di);
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

        Page<RegistrationDto> out = new Page<>(rrPage.getCurrent(), rrPage.getSize(), rrPage.getTotal());
        out.setRecords(outRecords);
        return out;
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

    private RegistrationRecord getActiveById(Long registrationId) {
        RegistrationRecord rr = registrationRecordRepository.selectById(registrationId);
        if (rr == null || (rr.getIsDeleted() != null && rr.getIsDeleted() != 0)) {
            throw new BizException(404, "挂号记录不存在");
        }
        return rr;
    }

    private int nextVisitSerialNumber(Long doctorId, LocalDate scheduleDate, String timeSlot) {
        QueryWrapper<RegistrationRecord> qw = new QueryWrapper<>();
        qw.eq("doctor_id", doctorId);
        qw.eq("schedule_date", scheduleDate);
        qw.eq("time_slot", timeSlot);
        qw.eq("is_deleted", 0);
        qw.in("registration_status", 0, 1);
        long cnt = registrationRecordRepository.selectCount(qw);
        return (int) cnt + 1;
    }

    private String generateRegistrationNo() {
        String ts = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rnd = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "REG" + ts + rnd;
    }

    private RegistrationDto toDto(RegistrationRecord rr, PatientInfo patient, DoctorInfo doctor) {
        RegistrationDto dto = new RegistrationDto();
        dto.setRegistrationId(rr.getRegistrationId());
        dto.setRegistrationNo(rr.getRegistrationNo());
        dto.setPatientId(rr.getPatientId());
        dto.setDoctorId(rr.getDoctorId());
        dto.setDeptId(rr.getDeptId());
        dto.setScheduleDate(rr.getScheduleDate());
        dto.setTimeSlot(rr.getTimeSlot());
        dto.setRegistrationFee(rr.getRegistrationFee());
        dto.setPayStatus(rr.getPayStatus());
        dto.setRegistrationStatus(rr.getRegistrationStatus());
        dto.setVisitSerialNumber(rr.getVisitSerialNumber());
        dto.setRemark(rr.getRemark());
        dto.setCreateTime(rr.getCreateTime());
        dto.setUpdateTime(rr.getUpdateTime());

        if (patient != null) {
            dto.setPatientUserId(patient.getUserId());
        }
        if (doctor != null) {
            dto.setDoctorUserId(doctor.getUserId());
        }

        return dto;
    }
}
