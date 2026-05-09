package com.app.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DoctorDto {

    private Long doctorId;

    private String username;

    private String realName;

    private Long deptId;

    private String jobTitle;

    private String specialty;

    private String introduction;

    private BigDecimal registrationFee;

    private String schedule;

    private Integer dailyAppointmentLimit;

    private Integer remainingAppointmentCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public Integer getDailyAppointmentLimit() {
        return dailyAppointmentLimit;
    }

    public void setDailyAppointmentLimit(Integer dailyAppointmentLimit) {
        this.dailyAppointmentLimit = dailyAppointmentLimit;
    }

    public Integer getRemainingAppointmentCount() {
        return remainingAppointmentCount;
    }

    public void setRemainingAppointmentCount(Integer remainingAppointmentCount) {
        this.remainingAppointmentCount = remainingAppointmentCount;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
