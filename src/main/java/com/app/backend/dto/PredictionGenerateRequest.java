package com.app.backend.dto;

import javax.validation.constraints.NotNull;

public class PredictionGenerateRequest {

    @NotNull
    private Long patientId;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }
}
