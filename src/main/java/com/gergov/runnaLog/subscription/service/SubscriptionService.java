package com.gergov.runnaLog.subscription.service;

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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final StatsRepository statsRepository;


    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository, StatsRepository statsRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.statsRepository = statsRepository;
    }


    @Transactional
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

        return true;
    }

    public boolean hasActiveEliteSubscription(User user) {

        if (user == null) {
            return false;
        }

        Subscription activeSubscription = subscriptionRepository.findLatestActiveByUser(user);

        return activeSubscription != null &&
                activeSubscription.getType() == SubscriptionType.ELITE &&
                activeSubscription.getStatus() == SubscriptionStatus.ACTIVE &&
                activeSubscription.getExpiryOn().isAfter(LocalDateTime.now());
        }

        public Subscription getActiveSubscription(User user) {

            if (user == null) {
                return null;
            }

            return subscriptionRepository.findLatestActiveByUser(user);
        }
    }
