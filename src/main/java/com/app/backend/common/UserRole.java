package com.app.backend.common;

public enum UserRole {
    PATIENT,
    DOCTOR,
    ADMIN;

    public static UserRole from(String value) {
        if (value == null || value.trim().isEmpty()) {
            return PATIENT;
        }
        try {
            return UserRole.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BizException(400, "非法角色");
        }
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isDoctor() {
        return this == DOCTOR;
    }

    public boolean isPatient() {
        return this == PATIENT;
    }
}
