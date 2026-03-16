package com.app.backend.dto;

import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;

public class QuestionnaireUpdateRequest {

    @NotNull
    private LocalDateTime questionnaireTime;

    @NotNull
    private Integer smoking;

    @NotNull
    private Integer drinking;

    @NotNull
    private Integer dietPreference;

    @NotNull
    private Integer exerciseFrequency;

    private Integer workRest;

    private Integer stressLevel;

    private String habitRemark;

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
}
