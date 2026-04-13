package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.HealthGuidanceCreateRequest;
import com.app.backend.dto.HealthGuidanceDto;
import com.app.backend.dto.HealthGuidanceUpdateRequest;
import com.app.backend.entity.HealthGuidance;
import com.app.backend.repository.HealthGuidanceRepository;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HealthGuidanceService {

    private static final DateTimeFormatter QUERY_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final HealthGuidanceRepository healthGuidanceRepository;
    private final PatientService patientService;
    private final UserService userService;
    private final PredictionResultService predictionResultService;

    public HealthGuidanceService(
            HealthGuidanceRepository healthGuidanceRepository,
            PatientService patientService,
            UserService userService,
            PredictionResultService predictionResultService
    ) {
        this.healthGuidanceRepository = healthGuidanceRepository;
        this.patientService = patientService;
        this.userService = userService;
        this.predictionResultService = predictionResultService;
    }

    @Transactional
    public HealthGuidanceDto create(Long doctorUserId, HealthGuidanceCreateRequest req) {
        patientService.getById(req.getPatientId());
        userService.getById(doctorUserId);
        if (req.getPredictionResultId() != null) {
            predictionResultService.getById(req.getPredictionResultId());
        }
        HealthGuidance guidance = new HealthGuidance();
        guidance.setPatientId(req.getPatientId());
        guidance.setDoctorUserId(doctorUserId);
        guidance.setPredictionResultId(req.getPredictionResultId());
        guidance.setGuidanceTitle(req.getGuidanceTitle());
        guidance.setGuidanceContent(req.getGuidanceContent());
        guidance.setGuidanceLevel(req.getGuidanceLevel());
        healthGuidanceRepository.insert(guidance);
        return toDto(guidance);
    }

    @Transactional
    public HealthGuidanceDto update(Long id, HealthGuidanceUpdateRequest req) {
        HealthGuidance guidance = getEntityById(id);
        if (req.getPredictionResultId() != null) {
            predictionResultService.getById(req.getPredictionResultId());
        }
        guidance.setPredictionResultId(req.getPredictionResultId());
        guidance.setGuidanceTitle(req.getGuidanceTitle());
        guidance.setGuidanceContent(req.getGuidanceContent());
        guidance.setGuidanceLevel(req.getGuidanceLevel());
        healthGuidanceRepository.updateById(guidance);
        return toDto(guidance);
    }

    @Transactional
    public void delete(Long id) {
        if (healthGuidanceRepository.selectById(id) == null) {
            throw new BizException(404, "健康指导不存在");
        }
        healthGuidanceRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public HealthGuidanceDto detail(Long id) {
        return toDto(getEntityById(id));
    }

    @Transactional(readOnly = true)
    public IPage<HealthGuidanceDto> page(int page, int size, Long patientId, String startTime, String endTime) {
        Page<HealthGuidance> p = new Page<>(Math.max(page, 0) + 1L, Math.min(Math.max(size, 1), 200));
        QueryWrapper<HealthGuidance> qw = new QueryWrapper<>();
        if (patientId != null) {
            qw.eq("patient_id", patientId);
        }
        if (startTime != null && !startTime.trim().isEmpty()) {
            qw.ge("created_at", LocalDateTime.parse(startTime, QUERY_TIME_FORMATTER));
        }
        if (endTime != null && !endTime.trim().isEmpty()) {
            qw.le("created_at", LocalDateTime.parse(endTime, QUERY_TIME_FORMATTER));
        }
        qw.orderByDesc("id");
        return healthGuidanceRepository.selectPage(p, qw).convert(this::toDto);
    }

    @Transactional(readOnly = true)
    public IPage<HealthGuidanceDto> pageByPatientId(int page, int size, Long patientId, String startTime, String endTime) {
        return page(page, size, patientId, startTime, endTime);
    }

    @Transactional(readOnly = true)
    public HealthGuidance getById(Long id) {
        return getEntityById(id);
    }

    private HealthGuidance getEntityById(Long id) {
        HealthGuidance guidance = healthGuidanceRepository.selectById(id);
        if (guidance == null) {
            throw new BizException(404, "健康指导不存在");
        }
        return guidance;
    }

    public HealthGuidanceDto toDto(HealthGuidance guidance) {
        HealthGuidanceDto dto = new HealthGuidanceDto();
        dto.setId(guidance.getId());
        dto.setPatientId(guidance.getPatientId());
        dto.setDoctorUserId(guidance.getDoctorUserId());
        dto.setPredictionResultId(guidance.getPredictionResultId());
        dto.setGuidanceTitle(guidance.getGuidanceTitle());
        dto.setGuidanceContent(guidance.getGuidanceContent());
        dto.setGuidanceLevel(guidance.getGuidanceLevel());
        dto.setCreatedAt(guidance.getCreatedAt());
        dto.setUpdatedAt(guidance.getUpdatedAt());
        return dto;
    }
}
