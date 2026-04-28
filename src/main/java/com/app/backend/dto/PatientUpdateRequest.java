package com.app.backend.dto;

import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class PatientUpdateRequest {

    @NotNull
    private Integer gender;

    @NotNull
    private Integer age;

    @NotNull
    private LocalDate birthDate;

    @Size(max = 32)
    private String phone;

    private Long organizationId;
    private String patientName;
    @Size(max = 128)
    private String medicalInstitution;

    @Size(max = 32)
    private String nation;

    @Size(max = 18)
    private String idCard;

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getMedicalInstitution() {
        return medicalInstitution;
    }

    public void setMedicalInstitution(String medicalInstitution) {
        this.medicalInstitution = medicalInstitution;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }
}
