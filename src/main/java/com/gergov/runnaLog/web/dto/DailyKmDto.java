package com.gergov.runnaLog.web.dto;

import java.util.UUID;

public record DailyKmDto(
        UUID userId,
        String username,
        double kilometers
) {}
