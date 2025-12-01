package com.gergov.runnaLog.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDto(
        UUID id,
        String username,
        String email,
        String role,
        boolean active,
        LocalDateTime createdOn

) {}

