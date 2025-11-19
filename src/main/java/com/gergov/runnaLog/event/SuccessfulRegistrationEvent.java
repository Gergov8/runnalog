package com.gergov.runnaLog.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SuccessfulRegistrationEvent {

    private UUID userId;

    private String email;

    private LocalDateTime createdOn;
}
