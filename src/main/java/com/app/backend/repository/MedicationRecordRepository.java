package com.app.backend.repository;

import com.app.backend.entity.MedicationRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MedicationRecordRepository extends BaseMapper<MedicationRecord> {
}
