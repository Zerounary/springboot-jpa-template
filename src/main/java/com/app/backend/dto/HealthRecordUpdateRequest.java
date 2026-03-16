package com.app.backend.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class HealthRecordUpdateRequest {

    @NotNull
    private Integer familyHypertension;

    @NotNull
    private Integer pastHypertension;

    @NotBlank
    @Size(max = 255)
    private String comorbidity;

    @NotBlank
    private String drugHistory;

    private String treatmentRecord;

    private String allergyHistory;

    public Integer getFamilyHypertension() {
        return familyHypertension;
    }

    public void setFamilyHypertension(Integer familyHypertension) {
        this.familyHypertension = familyHypertension;
    }

    public Integer getPastHypertension() {
        return pastHypertension;
    }

    public void setPastHypertension(Integer pastHypertension) {
        this.pastHypertension = pastHypertension;
    }

    public String getComorbidity() {
        return comorbidity;
    }

    public void setComorbidity(String comorbidity) {
        this.comorbidity = comorbidity;
    }

    public String getDrugHistory() {
        return drugHistory;
    }

    public void setDrugHistory(String drugHistory) {
        this.drugHistory = drugHistory;
    }

    public String getTreatmentRecord() {
        return treatmentRecord;
    }

    public void setTreatmentRecord(String treatmentRecord) {
        this.treatmentRecord = treatmentRecord;
    }

    public String getAllergyHistory() {
        return allergyHistory;
    }

    public void setAllergyHistory(String allergyHistory) {
        this.allergyHistory = allergyHistory;
    }
}
