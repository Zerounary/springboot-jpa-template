package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.MlModelDto;
import com.app.backend.entity.MlModel;
import com.app.backend.repository.MlModelRepository;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MlModelService {

    private final MlModelRepository mlModelRepository;

    public MlModelService(MlModelRepository mlModelRepository) {
        this.mlModelRepository = mlModelRepository;
    }

    @Transactional(readOnly = true)
    public List<MlModelDto> list(Boolean activeOnly) {
        QueryWrapper<MlModel> qw = new QueryWrapper<>();
        if (Boolean.TRUE.equals(activeOnly)) {
            qw.eq("is_active", 1);
        }
        qw.orderByDesc("trained_at").orderByDesc("id");
        return mlModelRepository.selectList(qw).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MlModel getById(Long id) {
        MlModel model = mlModelRepository.selectById(id);
        if (model == null) {
            throw new BizException(404, "模型不存在");
        }
        return model;
    }

    @Transactional(readOnly = true)
    public MlModel getLatestActive() {
        MlModel model = mlModelRepository.selectOne(new QueryWrapper<MlModel>()
                .eq("is_active", 1)
                .orderByDesc("trained_at")
                .orderByDesc("id")
                .last("LIMIT 1"));
        if (model == null) {
            throw new BizException(400, "暂无可用模型，请先训练模型");
        }
        return model;
    }

    @Transactional(readOnly = true)
    public MlModelDto detail(Long id) {
        return toDto(getById(id));
    }

    @Transactional
    public MlModel save(MlModel model) {
        mlModelRepository.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<MlModel>().set("is_active", 0));
        model.setIsActive(1);
        mlModelRepository.insert(model);
        return model;
    }

    public MlModelDto toDto(MlModel model) {
        MlModelDto dto = new MlModelDto();
        dto.setId(model.getId());
        dto.setModelName(model.getModelName());
        dto.setVersionTag(model.getVersionTag());
        dto.setAlgorithm(model.getAlgorithm());
        dto.setFeatureColumns(model.getFeatureColumns());
        dto.setModelPath(model.getModelPath());
        dto.setParamJson(model.getParamJson());
        dto.setMetricJson(model.getMetricJson());
        dto.setFeatureImportanceJson(model.getFeatureImportanceJson());
        dto.setTotalCount(model.getTotalCount());
        dto.setTrainCount(model.getTrainCount());
        dto.setTestCount(model.getTestCount());
        dto.setAuc(model.getAuc());
        dto.setAccuracy(model.getAccuracy());
        dto.setTrainedAt(model.getTrainedAt());
        dto.setIsActive(model.getIsActive());
        dto.setCreatedAt(model.getCreatedAt());
        dto.setUpdatedAt(model.getUpdatedAt());
        return dto;
    }
}
