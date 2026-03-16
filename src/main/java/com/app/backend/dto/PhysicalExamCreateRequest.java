package com.app.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;

public class PhysicalExamCreateRequest {

    @NotNull
    private Long patientId;

    @NotNull
    private LocalDateTime examTime;

    @NotNull
    private Integer systolicBp;

    @NotNull
    private Integer diastolicBp;

    @NotNull
    private BigDecimal bmi;

    @NotNull
    private BigDecimal cholesterol;

    @NotNull
    private BigDecimal fastingBloodSugar;

    @NotNull
    private BigDecimal height;

    @NotNull
    private BigDecimal weight;

    private Integer heartRate;

    private BigDecimal liverFunction;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public LocalDateTime getExamTime() {
        return examTime;
    }

    public void setExamTime(LocalDateTime examTime) {
        this.examTime = examTime;
    }

    public Integer getSystolicBp() {
        return systolicBp;
    }

    public void setSystolicBp(Integer systolicBp) {
        this.systolicBp = systolicBp;
    }

    public Integer getDiastolicBp() {
        return diastolicBp;
    }

    public void setDiastolicBp(Integer diastolicBp) {
        this.diastolicBp = diastolicBp;
    }

    public BigDecimal getBmi() {
        return bmi;
    }

    public void setBmi(BigDecimal bmi) {
        this.bmi = bmi;
    }

    public BigDecimal getCholesterol() {
        return cholesterol;
    }

    public void setCholesterol(BigDecimal cholesterol) {
        this.cholesterol = cholesterol;
    }

    public BigDecimal getFastingBloodSugar() {
        return fastingBloodSugar;
    }

    public void setFastingBloodSugar(BigDecimal fastingBloodSugar) {
        this.fastingBloodSugar = fastingBloodSugar;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public BigDecimal getLiverFunction() {
        return liverFunction;
    }

    public void setLiverFunction(BigDecimal liverFunction) {
        this.liverFunction = liverFunction;
    }
}
