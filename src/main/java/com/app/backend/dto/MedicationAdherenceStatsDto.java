package com.app.backend.dto;

import lombok.Data;

@Data
public class MedicationAdherenceStatsDto {
    private Integer expectedCount;  // Expected total doses
    private Integer takenCount;     // Actually taken doses
    private Integer missedCount;    // Missed doses
    private Integer rate;           // Adherence rate percentage
}
