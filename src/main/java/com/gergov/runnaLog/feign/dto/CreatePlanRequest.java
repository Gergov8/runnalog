package com.gergov.runnaLog.feign.dto;

import com.gergov.runnaLog.subscription.model.SubscriptionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreatePlanRequest {

    private UUID userId;
    private Double distanceKm;
    private SubscriptionType planLevel;
    private Integer daysPerWeek;

    // This getter returns the SubscriptionType enum
    public SubscriptionType getPlanLevel() {
        return planLevel;
    }
}