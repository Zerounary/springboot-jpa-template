package com.app.backend.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class DoctorCreateRequest {

    @NotBlank
    @Size(max = 64)
    private String username;

    @NotBlank
    @Size(min = 6, max = 64)
    private String password;

    @NotBlank
    @Size(max = 50)
    private String realName;

    @NotNull
    private Long deptId;

    @NotBlank
    @Size(max = 30)
    private String jobTitle;

    @NotBlank
    @Size(max = 255)
    private String specialty;

    private String introduction;

    @NotNull
    private BigDecimal registrationFee;

    @NotNull
    private Integer dailyAppointmentLimit;

    @Size(max = 255)
    private String schedule;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public BigDecimal getRegistrationFee() {
        return registrationFee;
    }

    public void setRegistrationFee(BigDecimal registrationFee) {
        this.registrationFee = registrationFee;
    }

    public Integer getDailyAppointmentLimit() {
        return dailyAppointmentLimit;
    }

    public void setDailyAppointmentLimit(Integer dailyAppointmentLimit) {
        this.dailyAppointmentLimit = dailyAppointmentLimit;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }
}
