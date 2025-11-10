package com.gergov.runnaLog.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.URL;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EditProfileRequest {

    @Size(min = 3, max = 25, message = "First name length must be between 3 and 25 symbols.")
    private String firstName;

    @Size(min = 0, max = 25, message = "Last name length must be between 3 and 25 symbols.")
    private String lastName;

    @URL
    private String profilePicture;
}
