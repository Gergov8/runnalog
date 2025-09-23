package com.gergov.runnaLog.subscription.repository;

import com.gergov.runnaLog.subscription.model.Subscription;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SubscriptionRepository extends CrudRepository<Subscription, UUID> {
}
