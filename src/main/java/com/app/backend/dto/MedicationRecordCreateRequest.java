package com.app.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MedicationRecordCreateRequest {
    private String prescriptionId;
    private String medicationName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime plannedTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime takenAt;
    
    private String dosage;
    private String frequency;
    private String status; // TAKEN, MISSED, SKIPPED
    private String notes;
}
