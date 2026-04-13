package com.app.backend.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class HealthGuidanceCreateRequest {

    @NotNull
    private Long patientId;

    private Long predictionResultId;

    @NotBlank
    @Size(max = 128)
    private String guidanceTitle;

    @NotBlank
    private String guidanceContent;

    @NotNull
    private Integer guidanceLevel;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getPredictionResultId() {
        return predictionResultId;
    }

    public void setPredictionResultId(Long predictionResultId) {
        this.predictionResultId = predictionResultId;
    }

    public String getGuidanceTitle() {
        return guidanceTitle;
    }

    public void setGuidanceTitle(String guidanceTitle) {
        this.guidanceTitle = guidanceTitle;
    }

    public String getGuidanceContent() {
        return guidanceContent;
    }

    public void setGuidanceContent(String guidanceContent) {
        this.guidanceContent = guidanceContent;
    }

    public Integer getGuidanceLevel() {
        return guidanceLevel;
    }

    public void setGuidanceLevel(Integer guidanceLevel) {
        this.guidanceLevel = guidanceLevel;
    }
}
