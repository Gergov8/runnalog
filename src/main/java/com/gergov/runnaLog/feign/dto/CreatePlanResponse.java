package com.gergov.runnaLog.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreatePlanResponse {

    UUID planId;
    private UUID userId;
    private String summary;
    private String planJson;

    public UUID getId() {
        return this.planId;
    }
}