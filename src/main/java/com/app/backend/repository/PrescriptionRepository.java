package com.app.backend.repository;

import com.app.backend.entity.Prescription;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrescriptionRepository extends BaseMapper<Prescription> {
}
