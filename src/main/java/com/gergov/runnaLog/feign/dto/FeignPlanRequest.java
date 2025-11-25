package com.gergov.runnaLog.feign.dto;

import java.util.UUID;

public record FeignPlanRequest(
        UUID userId,
        Double distanceKm,
        String planLevel,
        int daysPerWeek
) {}