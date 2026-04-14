package com.app.backend.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class DoctorAppointmentLimitUpdateRequest {
    @NotNull(message = "Daily appointment limit is required")
    @Min(value = 1, message = "Daily appointment limit must be at least 1")
    private Integer dailyAppointmentLimit;
}
