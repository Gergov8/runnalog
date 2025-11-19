package com.gergov.runnaLog.subscription.repository;

import com.gergov.runnaLog.subscription.model.Subscription;
import com.gergov.runnaLog.user.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SubscriptionRepository extends CrudRepository<Subscription, UUID> {

    @Query("SELECT s FROM Subscription s WHERE s.user = :user ORDER BY s.createdOn DESC LIMIT 1")
    Subscription findLatestByUser(@Param("user") User user);
}
