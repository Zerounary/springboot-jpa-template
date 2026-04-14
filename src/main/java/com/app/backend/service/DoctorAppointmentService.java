package com.app.backend.service;

import com.app.backend.dto.DoctorAppointmentLimitUpdateRequest;
import com.app.backend.dto.DoctorDailyAppointmentDto;
import com.app.backend.entity.DoctorDailyAppointment;
import com.app.backend.entity.DoctorInfo;
import com.app.backend.entity.RegistrationRecord;
import com.app.backend.repository.DoctorDailyAppointmentRepository;
import com.app.backend.repository.DoctorInfoRepository;
import com.app.backend.repository.RegistrationRecordRepository;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorAppointmentService {

    private final DoctorInfoRepository doctorInfoRepository;
    private final DoctorDailyAppointmentRepository doctorDailyAppointmentRepository;
    private final RegistrationRecordRepository registrationRecordRepository;

    public void updateDoctorAppointmentLimit(Long doctorId, DoctorAppointmentLimitUpdateRequest request) {
        DoctorInfo doctor = doctorInfoRepository.selectById(doctorId);
        if (doctor == null) {
            throw new RuntimeException("Doctor not found");
        }

        doctor.setDailyAppointmentLimit(request.getDailyAppointmentLimit());
        doctorInfoRepository.updateById(doctor);

        // Update existing daily appointments or create new ones
        List<DoctorDailyAppointment> existingAppointments = doctorDailyAppointmentRepository.selectList(
            new QueryWrapper<DoctorDailyAppointment>()
                .eq("doctor_id", doctorId)
                .ge("appointment_date", LocalDate.now())
        );

        for (DoctorDailyAppointment appointment : existingAppointments) {
            appointment.setDailyLimit(request.getDailyAppointmentLimit());
            appointment.setRemainingCount(request.getDailyAppointmentLimit() - appointment.getBookedCount());
            doctorDailyAppointmentRepository.updateById(appointment);
        }
    }

    public DoctorDailyAppointmentDto getDoctorDailyAppointment(Long doctorId, LocalDate date) {
        DoctorInfo doctor = doctorInfoRepository.selectById(doctorId);
        if (doctor == null) {
            throw new RuntimeException("Doctor not found");
        }

        DoctorDailyAppointment dailyAppointment = doctorDailyAppointmentRepository.selectOne(
            new QueryWrapper<DoctorDailyAppointment>()
                .eq("doctor_id", doctorId)
                .eq("appointment_date", date)
        );

        if (dailyAppointment == null) {
            // Create new daily appointment record
            dailyAppointment = new DoctorDailyAppointment();
            dailyAppointment.setDoctorId(doctorId);
            dailyAppointment.setAppointmentDate(date);
            dailyAppointment.setDailyLimit(doctor.getDailyAppointmentLimit() != null ? doctor.getDailyAppointmentLimit() : 20);
            
            // Count existing registrations for this date
            Integer bookedCount = Math.toIntExact(registrationRecordRepository.selectCount(
                new QueryWrapper<RegistrationRecord>()
                    .eq("doctor_id", doctorId)
                    .eq("schedule_date", date)
                    .in("registration_status", 0, 1) // Pending or Confirmed
            ));
            
            dailyAppointment.setBookedCount(bookedCount);
            dailyAppointment.setRemainingCount(dailyAppointment.getDailyLimit() - bookedCount);
            doctorDailyAppointmentRepository.insert(dailyAppointment);
        }

        return convertToDto(dailyAppointment, doctor);
    }

    public List<DoctorDailyAppointmentDto> getDoctorWeeklyAppointments(Long doctorId, LocalDate startDate) {
        List<DoctorDailyAppointmentDto> result = new ArrayList<>();
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            result.add(getDoctorDailyAppointment(doctorId, date));
        }
        
        return result;
    }

    public boolean checkAppointmentAvailability(Long doctorId, LocalDate date) {
        DoctorDailyAppointmentDto appointment = getDoctorDailyAppointment(doctorId, date);
        return appointment.getRemainingCount() > 0;
    }

    public void decrementAppointmentCount(Long doctorId, LocalDate date) {
        DoctorDailyAppointment appointment = doctorDailyAppointmentRepository.selectOne(
            new QueryWrapper<DoctorDailyAppointment>()
                .eq("doctor_id", doctorId)
                .eq("appointment_date", date)
        );

        if (appointment != null) {
            appointment.setBookedCount(appointment.getBookedCount() + 1);
            appointment.setRemainingCount(appointment.getRemainingCount() - 1);
            doctorDailyAppointmentRepository.updateById(appointment);
        }
    }

    public void incrementAppointmentCount(Long doctorId, LocalDate date) {
        DoctorDailyAppointment appointment = doctorDailyAppointmentRepository.selectOne(
            new QueryWrapper<DoctorDailyAppointment>()
                .eq("doctor_id", doctorId)
                .eq("appointment_date", date)
        );

        if (appointment != null && appointment.getBookedCount() > 0) {
            appointment.setBookedCount(appointment.getBookedCount() - 1);
            appointment.setRemainingCount(appointment.getRemainingCount() + 1);
            doctorDailyAppointmentRepository.updateById(appointment);
        }
    }

    private DoctorDailyAppointmentDto convertToDto(DoctorDailyAppointment appointment, DoctorInfo doctor) {
        DoctorDailyAppointmentDto dto = new DoctorDailyAppointmentDto();
        dto.setId(appointment.getId());
        dto.setDoctorId(appointment.getDoctorId());
        dto.setDoctorName(doctor != null ? getDoctorName(doctor.getUserId()) : "Unknown");
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setDailyLimit(appointment.getDailyLimit());
        dto.setBookedCount(appointment.getBookedCount());
        dto.setRemainingCount(appointment.getRemainingCount());
        return dto;
    }

    private String getDoctorName(Long userId) {
        // This would typically fetch from user repository
        // For simplicity, returning a placeholder
        return "Doctor " + userId;
    }
}
