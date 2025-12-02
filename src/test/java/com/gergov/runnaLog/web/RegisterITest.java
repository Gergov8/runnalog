package com.gergov.runnaLog.web;

import com.gergov.runnaLog.stats.model.Stats;
import com.gergov.runnaLog.stats.repository.StatsRepository;
import com.gergov.runnaLog.subscription.model.Subscription;
import com.gergov.runnaLog.subscription.model.SubscriptionStatus;
import com.gergov.runnaLog.subscription.repository.SubscriptionRepository;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.model.UserCountry;
import com.gergov.runnaLog.user.repository.UserRepository;
import com.gergov.runnaLog.user.service.UserService;
import com.gergov.runnaLog.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class RegisterITest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StatsRepository statsRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Test
    void registerUser_createsUserWithDefaultStatsAndSubscription() {

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("JohnDoe")
                .password("123456")
                .country(UserCountry.BULGARIA)
                .email("john@example.com")
                .build();

        User registeredUser = userService.register(registerRequest);

        Optional<User> userFromDb = userRepository.findById(registeredUser.getId());
        assertTrue(userFromDb.isPresent());
        assertEquals("JohnDoe", userFromDb.get().getUsername());
        assertEquals("john@example.com", userFromDb.get().getEmail());
        assertEquals(UserCountry.BULGARIA, userFromDb.get().getCountry());

        Optional<Stats> statsOptional = statsRepository.findByUserId(registeredUser.getId());
        assertTrue(statsOptional.isPresent());
        Stats stats = statsOptional.get();
        assertEquals(0, stats.getTotalRuns());
        assertEquals(0, stats.getTotalDistance());
        assertEquals(0, stats.getTotalDuration());
        assertEquals(100, stats.getStrides());

        Optional<Subscription> defaultSubscription = subscriptionRepository.findByUserIdAndStatus(registeredUser.getId(), SubscriptionStatus.ACTIVE);
        assertTrue(defaultSubscription.isPresent());
    }
}