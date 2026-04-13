package com.app.backend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("ml_models")
public class MlModel {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("model_name")
    private String modelName;

    @TableField("version_tag")
    private String versionTag;

    private String algorithm;

    @TableField("feature_columns")
    private String featureColumns;

    @TableField("model_path")
    private String modelPath;

    @TableField("param_json")
    private String paramJson;

    @TableField("metric_json")
    private String metricJson;

    @TableField("feature_importance_json")
    private String featureImportanceJson;

    @TableField("total_count")
    private Long totalCount;

    @TableField("train_count")
    private Long trainCount;

    @TableField("test_count")
    private Long testCount;

    private BigDecimal auc;

    private BigDecimal accuracy;

    @TableField("trained_at")
    private LocalDateTime trainedAt;

    @TableField("is_active")
    private Integer isActive;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
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
