package com.app.backend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("health_guidances")
public class HealthGuidance {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("patient_id")
    private Long patientId;

    @TableField("doctor_user_id")
    private Long doctorUserId;

    @TableField("prediction_result_id")
    private Long predictionResultId;

    @TableField("guidance_title")
    private String guidanceTitle;

    @TableField("guidance_content")
    private String guidanceContent;

    @TableField("guidance_level")
    private Integer guidanceLevel;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorUserId() {
        return doctorUserId;
    }

    public void setDoctorUserId(Long doctorUserId) {
        this.doctorUserId = doctorUserId;
    }

    public Long getPredictionResultId() {
        return predictionResultId;
    }

    public void setPredictionResultId(Long predictionResultId) {
        this.predictionResultId = predictionResultId;
    }

    public String getGuidanceTitle() {
        return guidanceTitle;
    }

    public void setGuidanceTitle(String guidanceTitle) {
        this.guidanceTitle = guidanceTitle;
    }

    public String getGuidanceContent() {
        return guidanceContent;
    }

    public void setGuidanceContent(String guidanceContent) {
        this.guidanceContent = guidanceContent;
    }

    public Integer getGuidanceLevel() {
        return guidanceLevel;
    }

    public void setGuidanceLevel(Integer guidanceLevel) {
        this.guidanceLevel = guidanceLevel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
