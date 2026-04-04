package com.app.backend.service;

import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    public String hash(String rawPassword) {
        return rawPassword;
    }

    public boolean matches(String rawPassword, String passwordHash) {
        return rawPassword.equals(passwordHash);
    }
}
