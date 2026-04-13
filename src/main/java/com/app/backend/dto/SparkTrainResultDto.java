package com.app.backend.dto;

public class SparkTrainResultDto {

    private Long total;
    private Long trainCount;
    private Long testCount;
    private Double auc;
    private Double accuracy;
    private Long modelId;
    private String versionTag;
    private String algorithm;
    private String modelPath;
    private String featureImportanceJson;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getTrainCount() {
        return trainCount;
    }

    public void setTrainCount(Long trainCount) {
        this.trainCount = trainCount;
    }

    public Long getTestCount() {
        return testCount;
    }

    public void setTestCount(Long testCount) {
        this.testCount = testCount;
    }

    public Double getAuc() {
        return auc;
    }

    public void setAuc(Double auc) {
        this.auc = auc;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getVersionTag() {
        return versionTag;
    }

    public void setVersionTag(String versionTag) {
        this.versionTag = versionTag;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public String getFeatureImportanceJson() {
        return featureImportanceJson;
    }

    public void setFeatureImportanceJson(String featureImportanceJson) {
        this.featureImportanceJson = featureImportanceJson;
    }
}
