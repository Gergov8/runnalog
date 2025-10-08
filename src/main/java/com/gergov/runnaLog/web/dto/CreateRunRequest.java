package com.gergov.runnaLog.web.dto;

import com.gergov.runnaLog.run.model.RunVisibility;
import jakarta.validation.constraints.*;

import java.time.Duration;

public class CreateRunRequest {

    @NotNull(message = "Distance is required.")
    @Positive(message = "Distance must be positive.")
    @Min(1)
    private Double distance;

    @NotNull(message = "Duration is required.")
    @Pattern(regexp = "^\\d{1,2}:\\d{2}(:\\d{2})?$", message = "Duration must be in format HH:MM:SS or MM:SS")
    private String duration;

    @NotBlank(message = "Title is required.")
    private String title;

    private String description;

    @NotNull(message = "Visibility is required.")
    private RunVisibility visibility;

    public CreateRunRequest() {
    }

    public CreateRunRequest(Double distance, String duration, String title, String description, RunVisibility visibility) {
        this.distance = distance;
        this.duration = duration;
        this.title = title;
        this.description = description;
        this.visibility = visibility;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RunVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(RunVisibility visibility) {
        this.visibility = visibility;
    }
}