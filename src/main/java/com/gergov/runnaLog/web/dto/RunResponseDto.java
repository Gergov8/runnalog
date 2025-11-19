package com.gergov.runnaLog.web.dto;



import java.util.UUID;

public record RunResponseDto(
        UUID id,
        UUID userId,
        String username,
        String profilePic,
        double distance,
        String duration,
        String pace,
        String title,
        int likesCount,
        boolean likedByCurrentUser

) {}

