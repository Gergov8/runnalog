package com.gergov.runnaLog.web.dto;

import com.gergov.runnaLog.run.model.RunVisibility;
import jakarta.validation.constraints.*;

import java.time.Duration;

public record CreateRunRequest(
        @NotNull(message = "Distance is required.")
        @Positive(message = "Distance must be positive.")
        @Min(1)
        Double distance,

        @Min(0)
        @Max(23)
        Integer hours,  // 0-23 hours

        @NotNull(message = "Minutes are required.")
        @Min(0)
        @Max(59)
        Integer minutes,  // 0-59 minutes

        @NotNull(message = "Seconds are required.")
        @Min(0)
        @Max(59)
        Integer seconds,  // 0-59 seconds


        @NotBlank(message = "Title is required.")
        String title,

        String description,

        @NotNull(message = "Visibility is required.")
        RunVisibility visibility
) {}