package com.gergov.runnaLog.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest{

        @Size(min = 6, max = 25, message = "Username length must be between 6 and 25 symbols.")
        @NotBlank(message = "Username is required.")
        private String username;

        @Size(min = 6, max = 6, message = "Password must be exactly 6 symbols.")
        @NotBlank(message = "Password is required.")
        private String password;

    public LoginRequest() {} // празен конструктор

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

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
}
