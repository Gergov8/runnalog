package com.gergov.runnaLog.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest{

        @Size(min = 6, max = 25, message = "Username length must be between 6 and 25 symbols.")
        @NotBlank(message = "Username is required.")
        private String username;

        @Size(min = 6, max = 6, message = "Password must be exactly 6 symbols.")
        @NotBlank(message = "Password is required.")
        private String password;
}
