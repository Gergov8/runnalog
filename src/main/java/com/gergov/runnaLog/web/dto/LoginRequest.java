package com.gergov.runnaLog.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @Size(min = 6, max = 25, message = "Username length must be between 6 and 25 symbols.")
        @NotBlank(message = "Username is required.")
        String username,

        @Size(min = 6, max = 6, message = "Password must be exactly 6 symbols.")
        @NotBlank(message = "Password is required.")
        String password

) {
}
