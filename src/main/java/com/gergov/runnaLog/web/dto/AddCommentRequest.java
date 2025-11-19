package com.gergov.runnaLog.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddCommentRequest {

    @Size(min = 1, max = 80, message = "Comment must be between 1 and 80 characters")
    @NotBlank(message = "Comment cannot be empty")
    private String content;
}
