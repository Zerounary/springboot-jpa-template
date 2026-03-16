package com.app.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class HypertensionFusionDto {

    private Long id;
    private Long patientId;
    private String userId;
    private Integer age;
    private Integer gender;
    private Integer systolicBp;
    private Integer diastolicBp;
    private BigDecimal bmi;
    private BigDecimal cholesterol;
    private Integer familyHypertension;
    private Integer smoking;
    private Integer dietPreference;
    private Integer hypertensionLabel;
    private LocalDateTime lastExamTime;
    private LocalDateTime lastQuestionnaireTime;
    private LocalDateTime createdAt;
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
