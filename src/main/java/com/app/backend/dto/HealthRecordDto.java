package com.app.backend.dto;

import java.time.LocalDateTime;

public class HealthRecordDto {

    private Long id;
    private Long patientId;
    private Integer familyHypertension;
    private Integer pastHypertension;
    private String comorbidity;
    private String drugHistory;
    private String treatmentRecord;
    private String allergyHistory;
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

    public Integer getFamilyHypertension() {
        return familyHypertension;
    }

    public void setFamilyHypertension(Integer familyHypertension) {
        this.familyHypertension = familyHypertension;
    }

    public Integer getPastHypertension() {
        return pastHypertension;
    }

    public void setPastHypertension(Integer pastHypertension) {
        this.pastHypertension = pastHypertension;
    }

    public String getComorbidity() {
        return comorbidity;
    }

    public void setComorbidity(String comorbidity) {
        this.comorbidity = comorbidity;
    }

    public String getDrugHistory() {
        return drugHistory;
    }

    public void setDrugHistory(String drugHistory) {
        this.drugHistory = drugHistory;
    }

    public String getTreatmentRecord() {
        return treatmentRecord;
    }

    public void setTreatmentRecord(String treatmentRecord) {
        this.treatmentRecord = treatmentRecord;
    }

    public String getAllergyHistory() {
        return allergyHistory;
    }

    public void setAllergyHistory(String allergyHistory) {
        this.allergyHistory = allergyHistory;
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
