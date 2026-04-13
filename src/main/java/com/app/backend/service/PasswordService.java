package com.app.backend.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    public String hash(String rawPassword) {
        // return BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray());
        return rawPassword;
    }

    public boolean matches(String rawPassword, String passwordHash) {
        // return BCrypt.verifyer().verify(rawPassword.toCharArray(), passwordHash).verified;
        return rawPassword.equals(passwordHash);
    }
}
