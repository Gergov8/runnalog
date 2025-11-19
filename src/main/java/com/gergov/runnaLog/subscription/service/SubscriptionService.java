package com.gergov.runnaLog.subscription.service;

import com.gergov.runnaLog.event.SuccessfulChargeEvent;
import com.gergov.runnaLog.stats.model.Stats;
import com.gergov.runnaLog.stats.repository.StatsRepository;
import com.gergov.runnaLog.subscription.model.Subscription;
import com.gergov.runnaLog.subscription.model.SubscriptionPeriod;
import com.gergov.runnaLog.subscription.model.SubscriptionStatus;
import com.gergov.runnaLog.subscription.model.SubscriptionType;
import com.gergov.runnaLog.subscription.repository.SubscriptionRepository;
import com.gergov.runnaLog.user.model.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final StatsRepository statsRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository, StatsRepository statsRepository, ApplicationEventPublisher eventPublisher) {
        this.subscriptionRepository = subscriptionRepository;
        this.statsRepository = statsRepository;
        this.eventPublisher = eventPublisher;
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
                .expiryOn(LocalDateTime.now().plusMonths(100))
                .build();

        subscriptionRepository.save(subscription);
    }

    @Transactional
    public boolean purchaseSubscription(User user, SubscriptionType type) {

        Stats stats = user.getStats();
        Subscription existingSubscription = subscriptionRepository.findLatestByUser(user);

        // Determine price based on type
        int price = switch (type) {
            case RECREATIONAL -> 0;
            case COMPETITIVE -> 6000;
            case ELITE -> 15000;
        };

        if (stats.getStrides() < price) {
            return false;
        }

        stats.setStrides(stats.getStrides() - price);
        statsRepository.save(stats);

        // Create and save subscription
        Subscription newSubscription = Subscription.builder()
                .user(user)
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(type)
                .price(price)
                .renewalAllowed(true)
                .createdOn(LocalDateTime.now())
                .expiryOn(LocalDateTime.now().plusMonths(1))
                .build();

        existingSubscription.setStatus(SubscriptionStatus.TERMINATED);
        existingSubscription.setExpiryOn(LocalDateTime.now());

        subscriptionRepository.save(newSubscription);

        SuccessfulChargeEvent event = SuccessfulChargeEvent.builder()
                .userId(user.getId())
                .type(type)
                .email(user.getEmail())
                .amount(price)
                .createdOn(LocalDateTime.now())
                .build();

        eventPublisher.publishEvent(event);

        return true;
    }



}
