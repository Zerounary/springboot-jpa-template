package com.app.backend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("hypertension_fusion")
public class HypertensionFusion {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("patient_id")
    private Long patientId;

    @TableField("user_id")
    private String userId;

    private Integer age;

    private Integer gender;

    @TableField("systolic_bp")
    private Integer systolicBp;

    @TableField("diastolic_bp")
    private Integer diastolicBp;

    private BigDecimal bmi;

    private BigDecimal cholesterol;

    @TableField("family_hypertension")
    private Integer familyHypertension;

    private Integer smoking;

    @TableField("diet_preference")
    private Integer dietPreference;

    @TableField("hypertension_label")
    private Integer hypertensionLabel;

    @TableField("last_exam_time")
    private LocalDateTime lastExamTime;

    @TableField("last_questionnaire_time")
    private LocalDateTime lastQuestionnaireTime;

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Integer getSystolicBp() {
        return systolicBp;
    }

    public void setSystolicBp(Integer systolicBp) {
        this.systolicBp = systolicBp;
    }

    public Integer getDiastolicBp() {
        return diastolicBp;
    }

    public void setDiastolicBp(Integer diastolicBp) {
        this.diastolicBp = diastolicBp;
    }

    public BigDecimal getBmi() {
        return bmi;
    }

    public void setBmi(BigDecimal bmi) {
        this.bmi = bmi;
    }

    public BigDecimal getCholesterol() {
        return cholesterol;
    }

    public void setCholesterol(BigDecimal cholesterol) {
        this.cholesterol = cholesterol;
    }

    public Integer getFamilyHypertension() {
        return familyHypertension;
    }

    public void setFamilyHypertension(Integer familyHypertension) {
        this.familyHypertension = familyHypertension;
    }

    public Integer getSmoking() {
        return smoking;
    }

    public void setSmoking(Integer smoking) {
        this.smoking = smoking;
    }

    public Integer getDietPreference() {
        return dietPreference;
    }

    public void setDietPreference(Integer dietPreference) {
        this.dietPreference = dietPreference;
    }

    public Integer getHypertensionLabel() {
        return hypertensionLabel;
    }

    public void setHypertensionLabel(Integer hypertensionLabel) {
        this.hypertensionLabel = hypertensionLabel;
    }

    public LocalDateTime getLastExamTime() {
        return lastExamTime;
    }

    public void setLastExamTime(LocalDateTime lastExamTime) {
        this.lastExamTime = lastExamTime;
    }

    public LocalDateTime getLastQuestionnaireTime() {
        return lastQuestionnaireTime;
    }

    public void setLastQuestionnaireTime(LocalDateTime lastQuestionnaireTime) {
        this.lastQuestionnaireTime = lastQuestionnaireTime;
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
