package com.app.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("prescription_items")
public class PrescriptionItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("prescription_id")
    private String prescriptionId;

    @TableField("medication_name")
    private String medicationName;

    @TableField("dosage")
    private String dosage;

    @TableField("frequency")
    private String frequency;

    @TableField("duration")
    private String duration;

    @TableField("note")
    private String note;

    @TableField("quantity")
    private Integer quantity;

    @TableField("unit")
    private String unit;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private Prescription prescription;
}
