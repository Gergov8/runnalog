package com.gergov.runnaLog.web.dto;

import com.gergov.runnaLog.run.model.RunVisibility;
import jakarta.validation.constraints.*;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateRunRequest {

    @NotNull(message = "Distance is required.")
    @Positive(message = "Distance must be positive.")
    @Min(1)
    private Double distance;

    @Min(0)
    @Max(24)
    private int durationHours;

    @Min(0)
    @Max(59)
    private int durationMinutes;

    @Min(0)
    @Max(59)
    private int durationSeconds;

    @NotBlank(message = "Title is required.")
    private String title;

    private String description;

    @NotNull(message = "Visibility is required.")
    private RunVisibility visibility;
}
