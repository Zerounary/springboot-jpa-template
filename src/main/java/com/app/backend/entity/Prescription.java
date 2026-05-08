package com.app.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("prescriptions")
public class Prescription {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("prescription_id")
    private String prescriptionId;

    @TableField("record_id")
    private Long recordId;

    @TableField("patient_id")
    private Long patientId;

    @TableField("doctor_id")
    private Long doctorId;

    @TableField("title")
    private String title;

    @TableField("treatment_plan")
    private String treatmentPlan;

    @TableField("visit_date")
    private LocalDateTime visitDate;

    @TableField("start_date")
    private LocalDateTime startDate;

    @TableField("end_date")
    private LocalDateTime endDate;

    @TableField("instructions")
    private String instructions;

    @TableField("status")
    private String status; // ACTIVE, COMPLETED, CANCELLED

    @TableField("reminder_times")
    private String reminderTimes; // JSON string of reminder times

    @TableField(exist = false)
    private List<PrescriptionItem> items;

    @TableField(exist = false)
    private List<MedicationRecord> medicationRecords;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
