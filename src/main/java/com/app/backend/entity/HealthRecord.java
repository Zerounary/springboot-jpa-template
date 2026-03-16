package com.app.backend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("health_records")
public class HealthRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("patient_id")
    private Long patientId;

    @TableField("family_hypertension")
    private Integer familyHypertension;

    @TableField("past_hypertension")
    private Integer pastHypertension;

    private String comorbidity;

    @TableField("drug_history")
    private String drugHistory;

    @TableField("treatment_record")
    private String treatmentRecord;

    @TableField("allergy_history")
    private String allergyHistory;

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
