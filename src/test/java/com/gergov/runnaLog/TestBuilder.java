package com.gergov.runnaLog;

import com.gergov.runnaLog.comment.model.Comment;
import com.gergov.runnaLog.like.model.Like;
import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.model.RunVisibility;
import com.gergov.runnaLog.stats.model.Stats;
import com.gergov.runnaLog.subscription.model.Subscription;
import com.gergov.runnaLog.subscription.model.SubscriptionPeriod;
import com.gergov.runnaLog.subscription.model.SubscriptionStatus;
import com.gergov.runnaLog.subscription.model.SubscriptionType;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.model.UserCountry;
import com.gergov.runnaLog.user.model.UserRole;
import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@UtilityClass
public class TestBuilder {

    public static User aRandomUser() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("User")
                .password("123123")
                .role(UserRole.USER)
                .country(UserCountry.BULGARIA)
                .active(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        long minutes = Duration.ofMinutes(55).getSeconds() / 60;
        long seconds = Duration.ofMinutes(55).getSeconds() % 60;
        String pace = String.format("%d:%02d", minutes, seconds);

        Run run = Run.builder()
                .id(UUID.randomUUID())
                .distance(10.00)
                .duration(Duration.ofMinutes(55))
                .pace(pace)
                .title("Test")
                .description("Test")
                .createdOn(LocalDateTime.now())
                .visibility(RunVisibility.PUBLIC)
                .user(user)
                .build();

        Like like = Like.builder()
                .id(UUID.randomUUID())
                .createdOn(LocalDateTime.now())
                .user(user)
                .run(run)
                .build();

        Comment comment = Comment.builder()
                .id(UUID.randomUUID())
                .content("Test")
                .createdOn(LocalDateTime.now())
                .user(user)
                .run(run)
                .build();

        Stats stats = Stats.builder()
                .id(UUID.randomUUID())
                .user(user)
                .totalRuns(user.getRuns().size())
                .totalDistance(run.getDistance())
                .totalDuration(run.getDuration().toMinutesPart())
                .pb1km(run.getPace())
                .pb5km(run.getPace())
                .pb10km(run.getPace())
                .build();

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

        return user;
    }
}
