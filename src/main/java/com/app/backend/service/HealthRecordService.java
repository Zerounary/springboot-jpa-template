package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.HealthRecordCreateRequest;
import com.app.backend.dto.HealthRecordDto;
import com.app.backend.dto.HealthRecordUpdateRequest;
import com.app.backend.entity.HealthRecord;
import com.app.backend.repository.HealthRecordRepository;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HealthRecordService {

    private static final DateTimeFormatter QUERY_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final HealthRecordRepository healthRecordRepository;
    private final PatientService patientService;

    public HealthRecordService(HealthRecordRepository healthRecordRepository, PatientService patientService) {
        this.healthRecordRepository = healthRecordRepository;
        this.patientService = patientService;
    }

    @Transactional
    public HealthRecordDto create(HealthRecordCreateRequest req) {
        patientService.getById(req.getPatientId());

        QueryWrapper<HealthRecord> qw = new QueryWrapper<>();
        qw.eq("patient_id", req.getPatientId());
        if (healthRecordRepository.selectCount(qw) > 0) {
            throw new BizException(400, "该患者健康档案已存在");
        }

        HealthRecord r = new HealthRecord();
        r.setPatientId(req.getPatientId());
        r.setFamilyHypertension(req.getFamilyHypertension());
        r.setPastHypertension(req.getPastHypertension());
        r.setComorbidity(req.getComorbidity());
        r.setDrugHistory(req.getDrugHistory());
        r.setTreatmentRecord(req.getTreatmentRecord());
        r.setAllergyHistory(req.getAllergyHistory());
        healthRecordRepository.insert(r);
        return toDto(r);
    }

    @Transactional
    public HealthRecordDto update(Long id, HealthRecordUpdateRequest req) {
        HealthRecord r = healthRecordRepository.selectById(id);
        if (r == null) {
            throw new BizException(404, "健康档案不存在");
        }
        r.setFamilyHypertension(req.getFamilyHypertension());
        r.setPastHypertension(req.getPastHypertension());
        r.setComorbidity(req.getComorbidity());
        r.setDrugHistory(req.getDrugHistory());
        r.setTreatmentRecord(req.getTreatmentRecord());
        r.setAllergyHistory(req.getAllergyHistory());
        healthRecordRepository.updateById(r);
        return toDto(r);
    }

    @Transactional
    public void delete(Long id) {
        if (healthRecordRepository.selectById(id) == null) {
            throw new BizException(404, "健康档案不存在");
        }
        healthRecordRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public HealthRecordDto detail(Long id) {
        HealthRecord r = healthRecordRepository.selectById(id);
        if (r == null) {
            throw new BizException(404, "健康档案不存在");
        }
        return toDto(r);
    }

    @Transactional(readOnly = true)
    public IPage<HealthRecordDto> page(int page, int size, Long patientId, String keyword, String startTime, String endTime) {
        Page<HealthRecord> p = new Page<>(Math.max(page, 0) + 1L, Math.min(Math.max(size, 1), 200));
        QueryWrapper<HealthRecord> qw = new QueryWrapper<>();
        if (patientId != null) {
            qw.eq("patient_id", patientId);
        }
        if (keyword != null) {
            qw.and(w -> w.like("comorbidity", keyword));
        }
        if (startTime != null && !startTime.trim().isEmpty()) {
            qw.ge("created_at", LocalDateTime.parse(startTime, QUERY_TIME_FORMATTER));
        }
        if (endTime != null && !endTime.trim().isEmpty()) {
            qw.le("created_at", LocalDateTime.parse(endTime, QUERY_TIME_FORMATTER));
        }
        qw.orderByDesc("id");
        IPage<HealthRecord> result = healthRecordRepository.selectPage(p, qw);
        return result.convert(this::toDto);
    }

    public HealthRecordDto toDto(HealthRecord r) {
        HealthRecordDto dto = new HealthRecordDto();
        dto.setId(r.getId());
        dto.setPatientId(r.getPatientId());
        dto.setFamilyHypertension(r.getFamilyHypertension());
        dto.setPastHypertension(r.getPastHypertension());
        dto.setComorbidity(r.getComorbidity());
        dto.setDrugHistory(r.getDrugHistory());
        dto.setTreatmentRecord(r.getTreatmentRecord());
        dto.setAllergyHistory(r.getAllergyHistory());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }
}
