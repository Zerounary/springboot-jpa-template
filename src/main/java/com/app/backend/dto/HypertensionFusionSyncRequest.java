package com.app.backend.dto;

import javax.validation.constraints.NotNull;

public class HypertensionFusionSyncRequest {

    @NotNull
    private Long patientId;

    private Integer hypertensionLabel;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Integer getHypertensionLabel() {
        return hypertensionLabel;
    }

    public void setHypertensionLabel(Integer hypertensionLabel) {
        this.hypertensionLabel = hypertensionLabel;
    }
}
