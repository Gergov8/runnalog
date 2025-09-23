package com.gergov.runnaLog.subscription.service;

import com.gergov.runnaLog.subscription.model.Subscription;
import com.gergov.runnaLog.subscription.model.SubscriptionPeriod;
import com.gergov.runnaLog.subscription.model.SubscriptionStatus;
import com.gergov.runnaLog.subscription.model.SubscriptionType;
import com.gergov.runnaLog.subscription.repository.SubscriptionRepository;
import com.gergov.runnaLog.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public void createDefaultSubscription(User user) {

        Subscription subscription = Subscription.builder()
                .user(user)
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.RECREATIONAL)
                .price(0)
                .renewalAllowed(true)
                .createdOn(LocalDateTime.now())
                .expiryOn(LocalDateTime.now().plusMonths(1))
                .build();

        subscriptionRepository.save(subscription);
    }
}
