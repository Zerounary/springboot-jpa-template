package com.app.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class PrescriptionUpdateRequest {
    private List<String> reminderTimes;
}
