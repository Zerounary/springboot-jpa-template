package com.app.backend.repository;

import com.app.backend.entity.PrescriptionItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrescriptionItemRepository extends BaseMapper<PrescriptionItem> {
}
