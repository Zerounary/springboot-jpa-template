package com.app.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DoctorDailyAppointmentDto {
    private Long id;
    private Long doctorId;
    private String doctorName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate appointmentDate;
    
    private Integer dailyLimit;
    private Integer bookedCount;
    private Integer remainingCount;
}
