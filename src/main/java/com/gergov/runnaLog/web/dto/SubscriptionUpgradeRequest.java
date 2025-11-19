package com.gergov.runnaLog.web.dto;

import com.gergov.runnaLog.subscription.model.SubscriptionType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionUpgradeRequest {

    @NotNull
    private SubscriptionType type;
}

