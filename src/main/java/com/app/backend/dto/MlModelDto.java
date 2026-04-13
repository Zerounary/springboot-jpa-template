package com.app.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MlModelDto {

    private Long id;
    private String modelName;
    private String versionTag;
    private String algorithm;
    private String featureColumns;
    private String modelPath;
    private String paramJson;
    private String metricJson;
    private String featureImportanceJson;
    private Long totalCount;
    private Long trainCount;
    private Long testCount;
    private BigDecimal auc;
    private BigDecimal accuracy;
    private LocalDateTime trainedAt;
    private Integer isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getVersionTag() { return versionTag; }
    public void setVersionTag(String versionTag) { this.versionTag = versionTag; }
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public String getFeatureColumns() { return featureColumns; }
    public void setFeatureColumns(String featureColumns) { this.featureColumns = featureColumns; }
    public String getModelPath() { return modelPath; }
    public void setModelPath(String modelPath) { this.modelPath = modelPath; }
    public String getParamJson() { return paramJson; }
    public void setParamJson(String paramJson) { this.paramJson = paramJson; }
    public String getMetricJson() { return metricJson; }
    public void setMetricJson(String metricJson) { this.metricJson = metricJson; }
    public String getFeatureImportanceJson() { return featureImportanceJson; }
    public void setFeatureImportanceJson(String featureImportanceJson) { this.featureImportanceJson = featureImportanceJson; }
    public Long getTotalCount() { return totalCount; }
    public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }
    public Long getTrainCount() { return trainCount; }
    public void setTrainCount(Long trainCount) { this.trainCount = trainCount; }
    public Long getTestCount() { return testCount; }
    public void setTestCount(Long testCount) { this.testCount = testCount; }
    public BigDecimal getAuc() { return auc; }
    public void setAuc(BigDecimal auc) { this.auc = auc; }
    public BigDecimal getAccuracy() { return accuracy; }
    public void setAccuracy(BigDecimal accuracy) { this.accuracy = accuracy; }
    public LocalDateTime getTrainedAt() { return trainedAt; }
    public void setTrainedAt(LocalDateTime trainedAt) { this.trainedAt = trainedAt; }
    public Integer getIsActive() { return isActive; }
    public void setIsActive(Integer isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
