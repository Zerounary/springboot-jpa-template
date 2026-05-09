package com.app.backend.dto;

import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class DoctorUpdateRequest {

    @Size(max = 64)
    private String username;

    @Size(max = 64)
    private String password;

    @Size(max = 50)
    private String realName;

    private Long deptId;

    @Size(max = 30)
    private String jobTitle;

    @Size(max = 255)
    private String specialty;

    private String introduction;

    private BigDecimal registrationFee;

    private Integer dailyAppointmentLimit;

    @Size(max = 255)
    private String schedule;

    private Integer isDeleted;

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

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}
