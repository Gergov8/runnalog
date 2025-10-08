package com.gergov.runnaLog.web.dto;

public record RunResponseDto(
        String username,
        double distance,
        String duration,
        String pace
) {}

