package com.app.backend.dto;

public class SparkTrainRequest {

    private String modelName;

    private Double testFraction;

    private Long seed;

    private Integer maxIter;

    private Integer numTrees;

    private Integer maxDepth;

    private Integer numFolds;

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

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

    public Integer getNumTrees() {
        return numTrees;
    }

    public void setNumTrees(Integer numTrees) {
        this.numTrees = numTrees;
    }

    public Integer getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(Integer maxDepth) {
        this.maxDepth = maxDepth;
    }

    public Integer getNumFolds() {
        return numFolds;
    }

    public void setNumFolds(Integer numFolds) {
        this.numFolds = numFolds;
    }
}
