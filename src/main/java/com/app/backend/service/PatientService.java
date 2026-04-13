package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.PatientCreateRequest;
import com.app.backend.dto.PatientDto;
import com.app.backend.dto.PatientUpdateRequest;
import com.app.backend.entity.Patient;
import com.app.backend.repository.PatientRepository;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    public PatientDto create(PatientCreateRequest req) {
        QueryWrapper<Patient> qw = new QueryWrapper<>();
        qw.eq("user_id", req.getUserId());
        if (patientRepository.selectCount(qw) > 0) {
            throw new BizException(400, "userId已存在");
        }
        Patient p = new Patient();
        p.setUserId(req.getUserId());
        p.setAccountId(req.getAccountId());
        p.setGender(req.getGender());
        p.setAge(req.getAge());
        p.setBirthDate(req.getBirthDate());
        p.setPhone(req.getPhone());
        p.setMedicalInstitution(req.getMedicalInstitution());
        p.setNation(req.getNation());
        patientRepository.insert(p);
        return toDto(p);
    }

    @Transactional
    public PatientDto update(Long id, PatientUpdateRequest req) {
        Patient p = patientRepository.selectById(id);
        if (p == null) {
            throw new BizException(404, "患者不存在");
        }
        p.setGender(req.getGender());
        p.setAge(req.getAge());
        p.setBirthDate(req.getBirthDate());
        if (req.getPhone() != null) {
            p.setPhone(req.getPhone());
        }
        if (req.getMedicalInstitution() != null) {
            p.setMedicalInstitution(req.getMedicalInstitution());
        }
        p.setNation(req.getNation());
        patientRepository.updateById(p);
        return toDto(p);
    }

    @Transactional
    public void delete(Long id) {
        if (patientRepository.selectById(id) == null) {
            throw new BizException(404, "患者不存在");
        }
        patientRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PatientDto detail(Long id) {
        Patient p = patientRepository.selectById(id);
        if (p == null) {
            throw new BizException(404, "患者不存在");
        }
        return toDto(p);
    }

    @Transactional(readOnly = true)
    public IPage<PatientDto> page(int page, int size, String keyword) {
        Page<Patient> p = new Page<>(Math.max(page, 0) + 1L, Math.min(Math.max(size, 1), 200));
        QueryWrapper<Patient> qw = new QueryWrapper<>();
        if (keyword != null) {
            qw.and(w -> w.like("user_id", keyword)
                    .or().like("phone", keyword)
                    .or().like("medical_institution", keyword));
        }
        qw.orderByDesc("id");
        IPage<Patient> result = patientRepository.selectPage(p, qw);
        return result.convert(this::toDto);
    }

    @Transactional(readOnly = true)
    public Patient getById(Long id) {
        Patient p = patientRepository.selectById(id);
        if (p == null) {
            throw new BizException(404, "患者不存在");
        }
        return p;
    }

    @Transactional(readOnly = true)
    public Patient getByAccountId(Long accountId) {
        QueryWrapper<Patient> qw = new QueryWrapper<>();
        qw.eq("account_id", accountId).last("LIMIT 1");
        Patient p = patientRepository.selectOne(qw);
        if (p == null) {
            throw new BizException(404, "当前账号未绑定患者档案");
        }
        return p;
    }

    @Transactional(readOnly = true)
    public PatientDto detailByAccountId(Long accountId) {
        return toDto(getByAccountId(accountId));
    }

    @Transactional
    public PatientDto updateByAccountId(Long accountId, PatientUpdateRequest req) {
        Patient p = getByAccountId(accountId);
        return update(p.getId(), req);
    }

    @Transactional(readOnly = true)
    public List<Long> listAllIds() {
        QueryWrapper<Patient> qw = new QueryWrapper<>();
        qw.select("id");
        return patientRepository.selectList(qw).stream().map(Patient::getId).collect(Collectors.toList());
    }

    public PatientDto toDto(Patient p) {
        PatientDto dto = new PatientDto();
        dto.setId(p.getId());
        dto.setUserId(p.getUserId());
        dto.setAccountId(p.getAccountId());
        dto.setGender(p.getGender());
        dto.setAge(p.getAge());
        dto.setBirthDate(p.getBirthDate());
        dto.setPhone(p.getPhone());
        dto.setMedicalInstitution(p.getMedicalInstitution());
        dto.setNation(p.getNation());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        return dto;
    }
}
