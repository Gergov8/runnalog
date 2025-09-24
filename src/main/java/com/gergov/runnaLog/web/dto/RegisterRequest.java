package com.gergov.runnaLog.web.dto;

import com.gergov.runnaLog.user.model.UserCountry;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest (

        @Size(min = 6, max = 25, message = "Username length must be between 6 and 25 symbols.")
        @NotBlank(message = "Username is required.")
        String username,

        @Email(message = "Please provide a valid email address.")
        @NotBlank(message = "Email is required.")
        String email,

        @Size(min = 6, max = 6, message = "Password must be exactly 6 symbols.")
        @NotBlank(message = "Password is required.")
        String password,

        @NotNull(message = "Country is required.")
        UserCountry country

) {
}



