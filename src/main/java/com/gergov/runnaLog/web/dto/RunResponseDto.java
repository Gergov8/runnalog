package com.gergov.runnaLog.web.dto;

import java.util.UUID;

public record RunResponseDto(
        UUID id,
        String username,
        double distance,
        String duration,
        String pace,
        String title,
        int likesCount,               // New field
        boolean likedByCurrentUser    // New field

) {}

