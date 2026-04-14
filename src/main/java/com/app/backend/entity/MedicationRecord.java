package com.app.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("medication_records")
public class MedicationRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("record_id")
    private String recordId;

    @TableField("prescription_id")
    private String prescriptionId;

    @TableField("patient_id")
    private Long patientId;

    @TableField("medication_name")
    private String medicationName;

    @TableField("planned_time")
    private LocalDateTime plannedTime;

    @TableField("taken_at")
    private LocalDateTime takenAt;

    @TableField("dosage")
    private String dosage;

    @TableField("frequency")
    private String frequency;

    @TableField("status")
    private String status; // TAKEN, MISSED, SKIPPED

    @TableField("notes")
    private String notes;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private Prescription prescription;
}
