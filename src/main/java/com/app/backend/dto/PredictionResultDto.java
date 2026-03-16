package com.app.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PredictionResultDto {

    private Long id;
    private Long patientId;
    private LocalDateTime predictionTime;
    private BigDecimal predictionProb;
    private Integer predictionLabel;
    private String coreRiskFactors;
    private Integer warningStatus;
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

    public LocalDateTime getPredictionTime() {
        return predictionTime;
    }

    public void setPredictionTime(LocalDateTime predictionTime) {
        this.predictionTime = predictionTime;
    }

    public BigDecimal getPredictionProb() {
        return predictionProb;
    }

    public void setPredictionProb(BigDecimal predictionProb) {
        this.predictionProb = predictionProb;
    }

    public Integer getPredictionLabel() {
        return predictionLabel;
    }

    public void setPredictionLabel(Integer predictionLabel) {
        this.predictionLabel = predictionLabel;
    }

    public String getCoreRiskFactors() {
        return coreRiskFactors;
    }

    public void setCoreRiskFactors(String coreRiskFactors) {
        this.coreRiskFactors = coreRiskFactors;
    }

    public Integer getWarningStatus() {
        return warningStatus;
    }

    public void setWarningStatus(Integer warningStatus) {
        this.warningStatus = warningStatus;
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
