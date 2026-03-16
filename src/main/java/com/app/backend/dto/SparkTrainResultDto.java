package com.app.backend.dto;

public class SparkTrainResultDto {

    private Long total;
    private Long trainCount;
    private Long testCount;
    private Double auc;
    private Double accuracy;

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
}
