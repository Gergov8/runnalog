package com.gergov.runnaLog.user.service;

import com.gergov.runnaLog.stats.model.Stats;
import com.gergov.runnaLog.stats.service.StatsService;
import com.gergov.runnaLog.subscription.service.SubscriptionService;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.model.UserRole;
import com.gergov.runnaLog.user.repository.UserRepository;
import com.gergov.runnaLog.web.dto.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StatsService statsService;
    private final SubscriptionService subscriptionService;


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, StatsService statsService, SubscriptionService subscriptionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.statsService = statsService;
        this.subscriptionService = subscriptionService;
    }

    public void register(RegisterRequest registerRequest) {

        Optional<User> optionalUserName = userRepository.findByUsername(registerRequest.username());
        if (optionalUserName.isPresent()) {
            throw new RuntimeException("User with [%s] username already exists.".formatted(registerRequest.username()));
        }

        Optional<User> optionalUserEmail = userRepository.findByEmail(registerRequest.email());
        if (optionalUserEmail.isPresent()) {
            throw new RuntimeException("User with [%s] email already exists.".formatted(registerRequest.email()));
        }

        User user = User.builder()
                .username(registerRequest.username())
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .role(UserRole.USER)
                .country(registerRequest.country())
                .active(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        user = userRepository.save(user);
        statsService.createDefaultStats(user);
        subscriptionService.createDefaultSubscription(user);

        log.info("New user profile was registered in the system for user [%s].".formatted(registerRequest.username()));
    }
}
