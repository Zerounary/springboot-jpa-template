package com.app.backend.dto;

import javax.validation.constraints.NotNull;

public class PredictionGenerateRequest {

    @NotNull
    private Long patientId;

    private Long modelId;

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
}
