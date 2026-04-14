package com.app.backend.controller;

import com.app.backend.common.ApiResponse;
import com.app.backend.config.AuthInterceptor;
import com.app.backend.dto.DoctorAppointmentLimitUpdateRequest;
import com.app.backend.dto.DoctorDailyAppointmentDto;
import com.app.backend.service.DoctorAppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/doctors")
public class DoctorAppointmentController {

    private final DoctorAppointmentService doctorAppointmentService;

    public DoctorAppointmentController(DoctorAppointmentService doctorAppointmentService) {
        this.doctorAppointmentService = doctorAppointmentService;
    }

    @PutMapping("/{doctorId}/appointment-limit")
    public ApiResponse<Void> updateAppointmentLimit(
            HttpServletRequest request,
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorAppointmentLimitUpdateRequest updateRequest) {
        // Only admin can update appointment limits
        Long userId = (Long) request.getAttribute(AuthInterceptor.REQ_ATTR_USER_ID);
        // TODO: Add role check for admin
        
        doctorAppointmentService.updateDoctorAppointmentLimit(doctorId, updateRequest);
        return ApiResponse.ok();
    }

    @GetMapping("/{doctorId}/daily-appointments")
    public ApiResponse<DoctorDailyAppointmentDto> getDailyAppointment(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        DoctorDailyAppointmentDto appointment = doctorAppointmentService.getDoctorDailyAppointment(doctorId, date);
        return ApiResponse.ok(appointment);
    }

    @GetMapping("/{doctorId}/weekly-appointments")
    public ApiResponse<java.util.List<DoctorDailyAppointmentDto>> getWeeklyAppointments(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate) {
        java.util.List<DoctorDailyAppointmentDto> appointments = doctorAppointmentService.getDoctorWeeklyAppointments(doctorId, startDate);
        return ApiResponse.ok(appointments);
    }

    @GetMapping("/{doctorId}/check-availability")
    public ApiResponse<Boolean> checkAvailability(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        boolean available = doctorAppointmentService.checkAppointmentAvailability(doctorId, date);
        return ApiResponse.ok(available);
    }
}
