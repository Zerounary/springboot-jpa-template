package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.PhysicalExamCreateRequest;
import com.app.backend.dto.PhysicalExamDto;
import com.app.backend.dto.PhysicalExamUpdateRequest;
import com.app.backend.entity.PhysicalExam;
import com.app.backend.repository.PhysicalExamRepository;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PhysicalExamService {

    private final PhysicalExamRepository physicalExamRepository;
    private final PatientService patientService;

    public PhysicalExamService(PhysicalExamRepository physicalExamRepository, PatientService patientService) {
        this.physicalExamRepository = physicalExamRepository;
        this.patientService = patientService;
    }

    @Transactional
    public PhysicalExamDto create(PhysicalExamCreateRequest req) {
        patientService.getById(req.getPatientId());

        PhysicalExam e = new PhysicalExam();
        e.setPatientId(req.getPatientId());
        e.setExamTime(req.getExamTime());
        e.setSystolicBp(req.getSystolicBp());
        e.setDiastolicBp(req.getDiastolicBp());
        e.setBmi(req.getBmi());
        e.setCholesterol(req.getCholesterol());
        e.setFastingBloodSugar(req.getFastingBloodSugar());
        e.setHeight(req.getHeight());
        e.setWeight(req.getWeight());
        e.setHeartRate(req.getHeartRate());
        e.setLiverFunction(req.getLiverFunction());
        physicalExamRepository.insert(e);
        return toDto(e);
    }

    @Transactional
    public PhysicalExamDto update(Long id, PhysicalExamUpdateRequest req) {
        PhysicalExam e = physicalExamRepository.selectById(id);
        if (e == null) {
            throw new BizException(404, "体检记录不存在");
        }
        e.setExamTime(req.getExamTime());
        e.setSystolicBp(req.getSystolicBp());
        e.setDiastolicBp(req.getDiastolicBp());
        e.setBmi(req.getBmi());
        e.setCholesterol(req.getCholesterol());
        e.setFastingBloodSugar(req.getFastingBloodSugar());
        e.setHeight(req.getHeight());
        e.setWeight(req.getWeight());
        e.setHeartRate(req.getHeartRate());
        e.setLiverFunction(req.getLiverFunction());
        physicalExamRepository.updateById(e);
        return toDto(e);
    }

    @Transactional
    public void delete(Long id) {
        if (physicalExamRepository.selectById(id) == null) {
            throw new BizException(404, "体检记录不存在");
        }
        physicalExamRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PhysicalExamDto detail(Long id) {
        PhysicalExam e = physicalExamRepository.selectById(id);
        if (e == null) {
            throw new BizException(404, "体检记录不存在");
        }
        return toDto(e);
    }

    @Transactional(readOnly = true)
    public IPage<PhysicalExamDto> page(int page, int size, Long patientId) {
        Page<PhysicalExam> p = new Page<>(Math.max(page, 0) + 1L, Math.min(Math.max(size, 1), 200));
        QueryWrapper<PhysicalExam> qw = new QueryWrapper<>();
        if (patientId != null) {
            qw.eq("patient_id", patientId);
        }
        qw.orderByDesc("exam_time").orderByDesc("id");
        IPage<PhysicalExam> result = physicalExamRepository.selectPage(p, qw);
        return result.convert(this::toDto);
    }

    @Transactional(readOnly = true)
    public IPage<PhysicalExamDto> pageByPatientId(int page, int size, Long patientId) {
        return page(page, size, patientId);
    }

    @Transactional(readOnly = true)
    public PhysicalExam getLatestByPatientId(Long patientId) {
        QueryWrapper<PhysicalExam> qw = new QueryWrapper<>();
        qw.eq("patient_id", patientId).orderByDesc("exam_time").orderByDesc("id").last("LIMIT 1");
        return physicalExamRepository.selectOne(qw);
    }

    public PhysicalExamDto toDto(PhysicalExam e) {
        PhysicalExamDto dto = new PhysicalExamDto();
        dto.setId(e.getId());
        dto.setPatientId(e.getPatientId());
        dto.setExamTime(e.getExamTime());
        dto.setSystolicBp(e.getSystolicBp());
        dto.setDiastolicBp(e.getDiastolicBp());
        dto.setBmi(e.getBmi());
        dto.setCholesterol(e.getCholesterol());
        dto.setFastingBloodSugar(e.getFastingBloodSugar());
        dto.setHeight(e.getHeight());
        dto.setWeight(e.getWeight());
        dto.setHeartRate(e.getHeartRate());
        dto.setLiverFunction(e.getLiverFunction());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }
}
