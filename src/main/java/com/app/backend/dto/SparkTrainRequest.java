package com.app.backend.dto;

public class SparkTrainRequest {

    private Double testFraction;

    private Long seed;

    private Integer maxIter;

    public Double getTestFraction() {
        return testFraction;
    }

    public void setTestFraction(Double testFraction) {
        this.testFraction = testFraction;
    }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public Integer getMaxIter() {
        return maxIter;
    }

    public void setMaxIter(Integer maxIter) {
        this.maxIter = maxIter;
    }
}
