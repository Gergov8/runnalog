package com.gergov.runnaLog.web.dto;

import com.gergov.runnaLog.run.model.RunVisibility;
import jakarta.validation.constraints.*;

import java.time.Duration;

public record CreateRunRequest(

        @NotNull(message = "Distance is required.")
        @Positive(message = "Distance must be positive.")
        @Min(1)
        Double distance,

        @NotNull(message = "Duration is required.")
        @Positive(message = "Duration must be positive.")
        Duration duration,

        @NotBlank(message = "Title is required.")
        String title,

        String description,

        @NotNull(message = "Visibility is required.")
        RunVisibility visibility
) {}