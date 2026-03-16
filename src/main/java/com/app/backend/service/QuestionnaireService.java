package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.QuestionnaireCreateRequest;
import com.app.backend.dto.QuestionnaireDto;
import com.app.backend.dto.QuestionnaireUpdateRequest;
import com.app.backend.entity.Questionnaire;
import com.app.backend.repository.QuestionnaireRepository;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionnaireService {

    private final QuestionnaireRepository questionnaireRepository;
    private final PatientService patientService;

    public QuestionnaireService(QuestionnaireRepository questionnaireRepository, PatientService patientService) {
        this.questionnaireRepository = questionnaireRepository;
        this.patientService = patientService;
    }

    @Transactional
    public QuestionnaireDto create(QuestionnaireCreateRequest req) {
        patientService.getById(req.getPatientId());

        Questionnaire q = new Questionnaire();
        q.setPatientId(req.getPatientId());
        q.setQuestionnaireTime(req.getQuestionnaireTime());
        q.setSmoking(req.getSmoking());
        q.setDrinking(req.getDrinking());
        q.setDietPreference(req.getDietPreference());
        q.setExerciseFrequency(req.getExerciseFrequency());
        q.setWorkRest(req.getWorkRest());
        q.setStressLevel(req.getStressLevel());
        q.setHabitRemark(req.getHabitRemark());
        questionnaireRepository.insert(q);
        return toDto(q);
    }

    @Transactional
    public QuestionnaireDto update(Long id, QuestionnaireUpdateRequest req) {
        Questionnaire q = questionnaireRepository.selectById(id);
        if (q == null) {
            throw new BizException(404, "问卷不存在");
        }
        q.setQuestionnaireTime(req.getQuestionnaireTime());
        q.setSmoking(req.getSmoking());
        q.setDrinking(req.getDrinking());
        q.setDietPreference(req.getDietPreference());
        q.setExerciseFrequency(req.getExerciseFrequency());
        q.setWorkRest(req.getWorkRest());
        q.setStressLevel(req.getStressLevel());
        q.setHabitRemark(req.getHabitRemark());
        questionnaireRepository.updateById(q);
        return toDto(q);
    }

    @Transactional
    public void delete(Long id) {
        if (questionnaireRepository.selectById(id) == null) {
            throw new BizException(404, "问卷不存在");
        }
        questionnaireRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public QuestionnaireDto detail(Long id) {
        Questionnaire q = questionnaireRepository.selectById(id);
        if (q == null) {
            throw new BizException(404, "问卷不存在");
        }
        return toDto(q);
    }

    @Transactional(readOnly = true)
    public IPage<QuestionnaireDto> page(int page, int size, Long patientId) {
        Page<Questionnaire> p = new Page<>(Math.max(page, 0) + 1L, Math.min(Math.max(size, 1), 200));
        QueryWrapper<Questionnaire> qw = new QueryWrapper<>();
        if (patientId != null) {
            qw.eq("patient_id", patientId);
        }
        qw.orderByDesc("questionnaire_time").orderByDesc("id");
        IPage<Questionnaire> result = questionnaireRepository.selectPage(p, qw);
        return result.convert(this::toDto);
    }

    public QuestionnaireDto toDto(Questionnaire q) {
        QuestionnaireDto dto = new QuestionnaireDto();
        dto.setId(q.getId());
        dto.setPatientId(q.getPatientId());
        dto.setQuestionnaireTime(q.getQuestionnaireTime());
        dto.setSmoking(q.getSmoking());
        dto.setDrinking(q.getDrinking());
        dto.setDietPreference(q.getDietPreference());
        dto.setExerciseFrequency(q.getExerciseFrequency());
        dto.setWorkRest(q.getWorkRest());
        dto.setStressLevel(q.getStressLevel());
        dto.setHabitRemark(q.getHabitRemark());
        dto.setCreatedAt(q.getCreatedAt());
        dto.setUpdatedAt(q.getUpdatedAt());
        return dto;
    }
}
