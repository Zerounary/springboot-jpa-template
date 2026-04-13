package com.app.backend.service;

import com.app.backend.common.BizException;
import com.app.backend.dto.OrganizationCreateRequest;
import com.app.backend.dto.OrganizationDto;
import com.app.backend.dto.OrganizationUpdateRequest;
import com.app.backend.entity.Organization;
import com.app.backend.repository.OrganizationRepository;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> list(String keyword) {
        QueryWrapper<Organization> qw = new QueryWrapper<>();
        if (keyword != null) {
            qw.and(w -> w.like("org_name", keyword).or().like("org_code", keyword));
        }
        qw.orderByAsc("id");
        return organizationRepository.selectList(qw).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public OrganizationDto create(OrganizationCreateRequest req) {
        QueryWrapper<Organization> qw = new QueryWrapper<>();
        qw.eq("org_name", req.getOrgName()).last("LIMIT 1");
        if (organizationRepository.selectOne(qw) != null) {
            throw new BizException(400, "机构名称已存在");
        }
        Organization o = new Organization();
        o.setOrgCode(req.getOrgCode());
        o.setOrgName(req.getOrgName());
        organizationRepository.insert(o);
        return toDto(o);
    }

    @Transactional
    public OrganizationDto update(Long id, OrganizationUpdateRequest req) {
        Organization o = organizationRepository.selectById(id);
        if (o == null) {
            throw new BizException(404, "机构不存在");
        }
        QueryWrapper<Organization> qw = new QueryWrapper<>();
        qw.eq("org_name", req.getOrgName()).ne("id", id).last("LIMIT 1");
        if (organizationRepository.selectOne(qw) != null) {
            throw new BizException(400, "机构名称已存在");
        }
        o.setOrgCode(req.getOrgCode());
        o.setOrgName(req.getOrgName());
        organizationRepository.updateById(o);
        return toDto(o);
    }

    @Transactional
    public void delete(Long id) {
        if (organizationRepository.selectById(id) == null) {
            throw new BizException(404, "机构不存在");
        }
        organizationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public OrganizationDto detail(Long id) {
        return toDto(getById(id));
    }

    @Transactional(readOnly = true)
    public Organization getById(Long id) {
        Organization o = organizationRepository.selectById(id);
        if (o == null) {
            throw new BizException(404, "机构不存在");
        }
        return o;
    }

    public OrganizationDto toDto(Organization o) {
        OrganizationDto dto = new OrganizationDto();
        dto.setId(o.getId());
        dto.setOrgCode(o.getOrgCode());
        dto.setOrgName(o.getOrgName());
        dto.setCreatedAt(o.getCreatedAt());
        dto.setUpdatedAt(o.getUpdatedAt());
        return dto;
    }
}
