package com.app.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class PredictionResultUpdateRequest {

    @NotNull
    private LocalDateTime predictionTime;

    @NotNull
    private BigDecimal predictionProb;

    @NotNull
    private Integer predictionLabel;

    @NotBlank
    private String coreRiskFactors;

    @NotNull
    private Integer warningStatus;

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
}
