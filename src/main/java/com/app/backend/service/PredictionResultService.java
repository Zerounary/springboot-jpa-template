package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.PredictionResultCreateRequest;
import com.app.backend.dto.PredictionResultDto;
import com.app.backend.dto.PredictionResultUpdateRequest;
import com.app.backend.entity.PredictionResult;
import com.app.backend.repository.PredictionResultRepository;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PredictionResultService {

    private final PredictionResultRepository predictionResultRepository;
    private final PatientService patientService;

    public PredictionResultService(PredictionResultRepository predictionResultRepository, PatientService patientService) {
        this.predictionResultRepository = predictionResultRepository;
        this.patientService = patientService;
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
