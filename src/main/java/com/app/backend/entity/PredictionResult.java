package com.app.backend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("prediction_results")
public class PredictionResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("patient_id")
    private Long patientId;

    @TableField("model_id")
    private Long modelId;

    @TableField("prediction_time")
    private LocalDateTime predictionTime;

    @TableField("prediction_prob")
    private BigDecimal predictionProb;

    @TableField("prediction_label")
    private Integer predictionLabel;

    @TableField("core_risk_factors")
    private String coreRiskFactors;

    @TableField("warning_status")
    private Integer warningStatus;

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

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
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
