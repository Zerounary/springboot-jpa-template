package com.app.backend.repository;

import com.app.backend.entity.DoctorDailyAppointment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DoctorDailyAppointmentRepository extends BaseMapper<DoctorDailyAppointment> {
}
