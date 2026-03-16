package com.app.backend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("questionnaires")
public class Questionnaire {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("patient_id")
    private Long patientId;

    @TableField("questionnaire_time")
    private LocalDateTime questionnaireTime;

    private Integer smoking;

    private Integer drinking;

    @TableField("diet_preference")
    private Integer dietPreference;

    @TableField("exercise_frequency")
    private Integer exerciseFrequency;

    @TableField("work_rest")
    private Integer workRest;

    @TableField("stress_level")
    private Integer stressLevel;

    @TableField("habit_remark")
    private String habitRemark;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public LocalDateTime getQuestionnaireTime() {
        return questionnaireTime;
    }

    public void setQuestionnaireTime(LocalDateTime questionnaireTime) {
        this.questionnaireTime = questionnaireTime;
    }

    public Integer getSmoking() {
        return smoking;
    }

    public void setSmoking(Integer smoking) {
        this.smoking = smoking;
    }

    public Integer getDrinking() {
        return drinking;
    }

    public void setDrinking(Integer drinking) {
        this.drinking = drinking;
    }

    public Integer getDietPreference() {
        return dietPreference;
    }

    public void setDietPreference(Integer dietPreference) {
        this.dietPreference = dietPreference;
    }

    public Integer getExerciseFrequency() {
        return exerciseFrequency;
    }

    public void setExerciseFrequency(Integer exerciseFrequency) {
        this.exerciseFrequency = exerciseFrequency;
    }

    public Integer getWorkRest() {
        return workRest;
    }

    public void setWorkRest(Integer workRest) {
        this.workRest = workRest;
    }

    public Integer getStressLevel() {
        return stressLevel;
    }

    public void setStressLevel(Integer stressLevel) {
        this.stressLevel = stressLevel;
    }

    public String getHabitRemark() {
        return habitRemark;
    }

    public void setHabitRemark(String habitRemark) {
        this.habitRemark = habitRemark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
