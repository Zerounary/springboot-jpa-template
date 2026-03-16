package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.HypertensionFusionDto;
import com.app.backend.dto.HypertensionFusionSyncAllResultDto;
import com.app.backend.entity.HealthRecord;
import com.app.backend.entity.HypertensionFusion;
import com.app.backend.entity.Patient;
import com.app.backend.entity.PhysicalExam;
import com.app.backend.entity.Questionnaire;
import com.app.backend.repository.HealthRecordRepository;
import com.app.backend.repository.HypertensionFusionRepository;
import com.app.backend.repository.PhysicalExamRepository;
import com.app.backend.repository.QuestionnaireRepository;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HypertensionFusionService {

    private final HypertensionFusionRepository fusionRepository;
    private final PatientService patientService;
    private final HealthRecordRepository healthRecordRepository;
    private final PhysicalExamRepository physicalExamRepository;
    private final QuestionnaireRepository questionnaireRepository;

    public HypertensionFusionService(
            HypertensionFusionRepository fusionRepository,
            PatientService patientService,
            HealthRecordRepository healthRecordRepository,
            PhysicalExamRepository physicalExamRepository,
            QuestionnaireRepository questionnaireRepository
    ) {
        this.fusionRepository = fusionRepository;
        this.patientService = patientService;
        this.healthRecordRepository = healthRecordRepository;
        this.physicalExamRepository = physicalExamRepository;
        this.questionnaireRepository = questionnaireRepository;
    }

    @Transactional
    public HypertensionFusionDto syncOne(Long patientId, Integer hypertensionLabel) {
        Patient patient = patientService.getById(patientId);

        HealthRecord hr = healthRecordRepository.selectOne(new QueryWrapper<HealthRecord>()
                .eq("patient_id", patientId)
                .last("LIMIT 1"));
        if (hr == null) {
            throw new BizException(400, "缺少健康档案");
        }

        PhysicalExam pe = physicalExamRepository.selectOne(new QueryWrapper<PhysicalExam>()
                .eq("patient_id", patientId)
                .orderByDesc("exam_time")
                .last("LIMIT 1"));
        if (pe == null) {
            throw new BizException(400, "缺少体检数据");
        }

        Questionnaire qn = questionnaireRepository.selectOne(new QueryWrapper<Questionnaire>()
                .eq("patient_id", patientId)
                .orderByDesc("questionnaire_time")
                .last("LIMIT 1"));
        if (qn == null) {
            throw new BizException(400, "缺少问卷数据");
        }

        Integer label = hypertensionLabel;
        if (label == null) {
            Integer past = hr.getPastHypertension();
            label = (past != null && past != 0) ? 1 : 0;
        }

        HypertensionFusion fusion = fusionRepository.selectOne(new QueryWrapper<HypertensionFusion>()
                .eq("patient_id", patientId)
                .last("LIMIT 1"));
        if (fusion == null) {
            fusion = new HypertensionFusion();
            fusion.setPatientId(patientId);
        }

        fusion.setUserId(patient.getUserId());
        fusion.setAge(patient.getAge());
        fusion.setGender(patient.getGender());
        fusion.setSystolicBp(pe.getSystolicBp());
        fusion.setDiastolicBp(pe.getDiastolicBp());
        fusion.setBmi(pe.getBmi());
        fusion.setCholesterol(pe.getCholesterol());
        fusion.setFamilyHypertension(hr.getFamilyHypertension());
        fusion.setSmoking(qn.getSmoking());
        fusion.setDietPreference(qn.getDietPreference());
        fusion.setHypertensionLabel(label);
        fusion.setLastExamTime(pe.getExamTime());
        fusion.setLastQuestionnaireTime(qn.getQuestionnaireTime());

        if (fusion.getId() == null) {
            fusionRepository.insert(fusion);
        } else {
            fusionRepository.updateById(fusion);
        }
        return toDto(fusion);
    }

    @Transactional
    public HypertensionFusionSyncAllResultDto syncAll() {
        List<Long> ids = patientService.listAllIds();
        int success = 0;
        int failed = 0;
        for (Long id : ids) {
            try {
                syncOne(id, null);
                success++;
            } catch (Exception e) {
                failed++;
            }
        }
        HypertensionFusionSyncAllResultDto dto = new HypertensionFusionSyncAllResultDto();
        dto.setTotal(ids.size());
        dto.setSuccess(success);
        dto.setFailed(failed);
        return dto;
    }

    @Transactional(readOnly = true)
    public HypertensionFusionDto detailByPatientId(Long patientId) {
        HypertensionFusion fusion = fusionRepository.selectOne(new QueryWrapper<HypertensionFusion>()
                .eq("patient_id", patientId)
                .last("LIMIT 1"));
        if (fusion == null) {
            throw new BizException(404, "融合数据不存在");
        }
        return toDto(fusion);
    }

    @Transactional(readOnly = true)
    public IPage<HypertensionFusionDto> page(int page, int size, String keyword) {
        Page<HypertensionFusion> p = new Page<>(Math.max(page, 0) + 1L, Math.min(Math.max(size, 1), 200));
        QueryWrapper<HypertensionFusion> qw = new QueryWrapper<>();
        if (keyword != null) {
            qw.and(w -> w.like("user_id", keyword));
        }
        qw.orderByDesc("id");
        IPage<HypertensionFusion> result = fusionRepository.selectPage(p, qw);
        return result.convert(this::toDto);
    }

    public HypertensionFusionDto toDto(HypertensionFusion f) {
        HypertensionFusionDto dto = new HypertensionFusionDto();
        dto.setId(f.getId());
        dto.setPatientId(f.getPatientId());
        dto.setUserId(f.getUserId());
        dto.setAge(f.getAge());
        dto.setGender(f.getGender());
        dto.setSystolicBp(f.getSystolicBp());
        dto.setDiastolicBp(f.getDiastolicBp());
        dto.setBmi(f.getBmi());
        dto.setCholesterol(f.getCholesterol());
        dto.setFamilyHypertension(f.getFamilyHypertension());
        dto.setSmoking(f.getSmoking());
        dto.setDietPreference(f.getDietPreference());
        dto.setHypertensionLabel(f.getHypertensionLabel());
        dto.setLastExamTime(f.getLastExamTime());
        dto.setLastQuestionnaireTime(f.getLastQuestionnaireTime());
        dto.setCreatedAt(f.getCreatedAt());
        dto.setUpdatedAt(f.getUpdatedAt());
        return dto;
    }
}
