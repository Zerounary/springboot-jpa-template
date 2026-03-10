package com.app.backend.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

public class UserUpdateRequest {

    @Size(max = 64)
    private String nickname;

    @Email
    @Size(max = 128)
    private String email;

    @Size(min = 6, max = 64)
    private String password;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
