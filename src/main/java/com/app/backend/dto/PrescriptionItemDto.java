package com.app.backend.dto;

import lombok.Data;

@Data
public class PrescriptionItemDto {
    private Long id;
    private String prescriptionId;
    private String medicationName;
    private String dosage;
    private String frequency;
    private String duration;
    private String note;
    private Integer quantity;
    private String unit;
}
