package com.gergov.runnaLog.event;

import com.gergov.runnaLog.subscription.model.SubscriptionType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SuccessfulChargeEvent {

    private UUID userId;

    private SubscriptionType type;

    private String email;

    private Integer amount;

    private LocalDateTime createdOn;
}
