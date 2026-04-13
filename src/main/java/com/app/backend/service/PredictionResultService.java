package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.PredictionResultCreateRequest;
import com.app.backend.dto.PredictionResultDto;
import com.app.backend.dto.PredictionResultUpdateRequest;
import com.app.backend.entity.HealthRecord;
import com.app.backend.entity.PhysicalExam;
import com.app.backend.entity.PredictionResult;
import com.app.backend.entity.Questionnaire;
import com.app.backend.repository.HealthRecordRepository;
import com.app.backend.repository.QuestionnaireRepository;
import com.app.backend.repository.PredictionResultRepository;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PredictionResultService {

    private final PredictionResultRepository predictionResultRepository;
    private final PatientService patientService;
    private final PhysicalExamService physicalExamService;
    private final HealthRecordRepository healthRecordRepository;
    private final QuestionnaireRepository questionnaireRepository;

    public PredictionResultService(
            PredictionResultRepository predictionResultRepository,
            PatientService patientService,
            PhysicalExamService physicalExamService,
            HealthRecordRepository healthRecordRepository,
            QuestionnaireRepository questionnaireRepository
    ) {
        this.predictionResultRepository = predictionResultRepository;
        this.patientService = patientService;
        this.physicalExamService = physicalExamService;
        this.healthRecordRepository = healthRecordRepository;
        this.questionnaireRepository = questionnaireRepository;
    }

    @Transactional
    public PredictionResultDto create(PredictionResultCreateRequest req) {
        patientService.getById(req.getPatientId());

        PredictionResult r = new PredictionResult();
        r.setPatientId(req.getPatientId());
        r.setPredictionTime(req.getPredictionTime());
        r.setPredictionProb(req.getPredictionProb());
        r.setPredictionLabel(req.getPredictionLabel());
        r.setCoreRiskFactors(req.getCoreRiskFactors());
        r.setWarningStatus(req.getWarningStatus());
        predictionResultRepository.insert(r);
        return toDto(r);
    }

    @Transactional
    public PredictionResultDto update(Long id, PredictionResultUpdateRequest req) {
        PredictionResult r = predictionResultRepository.selectById(id);
        if (r == null) {
            throw new BizException(404, "预测结果不存在");
        }
        r.setPredictionTime(req.getPredictionTime());
        r.setPredictionProb(req.getPredictionProb());
        r.setPredictionLabel(req.getPredictionLabel());
        r.setCoreRiskFactors(req.getCoreRiskFactors());
        r.setWarningStatus(req.getWarningStatus());
        predictionResultRepository.updateById(r);
        return toDto(r);
    }

    @Transactional
    public void delete(Long id) {
        if (predictionResultRepository.selectById(id) == null) {
            throw new BizException(404, "预测结果不存在");
        }
        predictionResultRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PredictionResultDto detail(Long id) {
        PredictionResult r = predictionResultRepository.selectById(id);
        if (r == null) {
            throw new BizException(404, "预测结果不存在");
        }
        return toDto(r);
    }

    @Transactional(readOnly = true)
    public IPage<PredictionResultDto> page(int page, int size, Long patientId) {
        Page<PredictionResult> p = new Page<>(Math.max(page, 0) + 1L, Math.min(Math.max(size, 1), 200));
        QueryWrapper<PredictionResult> qw = new QueryWrapper<>();
        if (patientId != null) {
            qw.eq("patient_id", patientId);
        }
        qw.orderByDesc("prediction_time").orderByDesc("id");
        IPage<PredictionResult> result = predictionResultRepository.selectPage(p, qw);
        return result.convert(this::toDto);
    }

    @Transactional(readOnly = true)
    public IPage<PredictionResultDto> pageByPatientId(int page, int size, Long patientId) {
        return page(page, size, patientId);
    }

    @Transactional(readOnly = true)
    public PredictionResult getById(Long id) {
        PredictionResult result = predictionResultRepository.selectById(id);
        if (result == null) {
            throw new BizException(404, "预测结果不存在");
        }
        return result;
    }

    @Transactional
    public PredictionResultDto generateByPatientId(Long patientId) {
        patientService.getById(patientId);
        PhysicalExam exam = physicalExamService.getLatestByPatientId(patientId);
        if (exam == null) {
            throw new BizException(400, "缺少体检数据，无法生成预测结果");
        }

        QueryWrapper<HealthRecord> healthRecordQuery = new QueryWrapper<>();
        healthRecordQuery.eq("patient_id", patientId).last("LIMIT 1");
        HealthRecord healthRecord = healthRecordRepository.selectOne(healthRecordQuery);

        QueryWrapper<Questionnaire> questionnaireQuery = new QueryWrapper<>();
        questionnaireQuery.eq("patient_id", patientId)
                .orderByDesc("questionnaire_time")
                .orderByDesc("id")
                .last("LIMIT 1");
        Questionnaire questionnaire = questionnaireRepository.selectOne(questionnaireQuery);

        int score = 0;
        List<String> riskFactors = new ArrayList<>();

        if (exam.getSystolicBp() != null && exam.getSystolicBp() >= 140) {
            score += 2;
            riskFactors.add("收缩压偏高");
        }
        if (exam.getDiastolicBp() != null && exam.getDiastolicBp() >= 90) {
            score += 2;
            riskFactors.add("舒张压偏高");
        }
        if (exam.getBmi() != null && exam.getBmi().compareTo(new BigDecimal("24.00")) >= 0) {
            score += 1;
            riskFactors.add("BMI偏高");
        }
        if (exam.getCholesterol() != null && exam.getCholesterol().compareTo(new BigDecimal("5.20")) >= 0) {
            score += 1;
            riskFactors.add("胆固醇偏高");
        }
        if (exam.getFastingBloodSugar() != null && exam.getFastingBloodSugar().compareTo(new BigDecimal("6.10")) >= 0) {
            score += 1;
            riskFactors.add("空腹血糖偏高");
        }
        if (healthRecord != null && Integer.valueOf(1).equals(healthRecord.getFamilyHypertension())) {
            score += 1;
            riskFactors.add("家族高血压史");
        }
        if (healthRecord != null && Integer.valueOf(1).equals(healthRecord.getPastHypertension())) {
            score += 1;
            riskFactors.add("既往高血压史");
        }
        if (questionnaire != null && questionnaire.getSmoking() != null && questionnaire.getSmoking() > 0) {
            score += 1;
            riskFactors.add("吸烟");
        }
        if (questionnaire != null && questionnaire.getDrinking() != null && questionnaire.getDrinking() > 0) {
            score += 1;
            riskFactors.add("饮酒");
        }
        if (questionnaire != null && questionnaire.getStressLevel() != null && questionnaire.getStressLevel() >= 2) {
            score += 1;
            riskFactors.add("压力水平较高");
        }

        BigDecimal probability = BigDecimal.valueOf(Math.min(0.95d, Math.max(0.05d, score / 10.0d)))
                .setScale(5, RoundingMode.HALF_UP);
        int label = probability.compareTo(new BigDecimal("0.50000")) >= 0 ? 1 : 0;
        int warningStatus = probability.compareTo(new BigDecimal("0.70000")) >= 0 ? 2 : (label == 1 ? 1 : 0);

        PredictionResult result = new PredictionResult();
        result.setPatientId(patientId);
        result.setPredictionTime(LocalDateTime.now());
        result.setPredictionProb(probability);
        result.setPredictionLabel(label);
        result.setCoreRiskFactors(riskFactors.isEmpty() ? "未识别明显高风险因素" : String.join("，", riskFactors));
        result.setWarningStatus(warningStatus);
        predictionResultRepository.insert(result);
        return toDto(result);
    }

    public PredictionResultDto toDto(PredictionResult r) {
        PredictionResultDto dto = new PredictionResultDto();
        dto.setId(r.getId());
        dto.setPatientId(r.getPatientId());
        dto.setPredictionTime(r.getPredictionTime());
        dto.setPredictionProb(r.getPredictionProb());
        dto.setPredictionLabel(r.getPredictionLabel());
        dto.setCoreRiskFactors(r.getCoreRiskFactors());
        dto.setWarningStatus(r.getWarningStatus());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }
}
