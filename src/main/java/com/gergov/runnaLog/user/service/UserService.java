package com.gergov.runnaLog.user.service;

import com.gergov.runnaLog.event.SuccessfulRegistrationEvent;
import com.gergov.runnaLog.run.repository.RunRepository;
import com.gergov.runnaLog.security.UserData;
import com.gergov.runnaLog.stats.service.StatsService;
import com.gergov.runnaLog.subscription.service.SubscriptionService;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.model.UserRole;
import com.gergov.runnaLog.user.repository.UserRepository;
import com.gergov.runnaLog.web.dto.DailyKmDto;
import com.gergov.runnaLog.web.dto.EditProfileRequest;
import com.gergov.runnaLog.web.dto.RegisterRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StatsService statsService;
    private final SubscriptionService subscriptionService;
    private final ApplicationEventPublisher eventPublisher;
    private final RunRepository runRepository;


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, StatsService statsService, SubscriptionService subscriptionService, ApplicationEventPublisher eventPublisher, RunRepository runRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.statsService = statsService;
        this.subscriptionService = subscriptionService;
        this.eventPublisher = eventPublisher;
        this.runRepository = runRepository;
    }

    @Transactional
    @CacheEvict(value = {"users", "usersById", "userDataByUsername"}, allEntries = true)
    public void register(RegisterRequest registerRequest) {

        Optional<User> optionalUserName = userRepository.findByUsername(registerRequest.getUsername());
        if (optionalUserName.isPresent()) {
            throw new RuntimeException("User with [%s] username already exists.".formatted(registerRequest.getUsername()));
        }

        Optional<User> optionalUserEmail = userRepository.findByEmail(registerRequest.getEmail());
        if (optionalUserEmail.isPresent()) {
            throw new RuntimeException("User with [%s] email already exists.".formatted(registerRequest.getEmail()));
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.USER)
                .country(registerRequest.getCountry())
                .active(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        user = userRepository.save(user);
        statsService.createDefaultStats(user);
        subscriptionService.createDefaultSubscription(user);

        SuccessfulRegistrationEvent event = SuccessfulRegistrationEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .createdOn(user.getCreatedOn())
                .build();

        eventPublisher.publishEvent(event);

        log.info("New user profile was registered in the system for user [%s].".formatted(registerRequest.getUsername()));
    }

    @Cacheable("users")
    public List<User> getAll() {

        return userRepository.findAll();
    }

    @Cacheable("usersById")
    public User getById(UUID id) {

        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User with [%s] id does not exist.".formatted(id)));
    }

    @CacheEvict(value = {"users", "usersById", "userDataByUsername"})
    public void updateUserProfile(UUID id, EditProfileRequest editProfileRequest) {
        User existingUser = getById(id);

        existingUser.setFirstName(editProfileRequest.getFirstName());
        existingUser.setLastName(editProfileRequest.getLastName());
        existingUser.setProfilePicture(editProfileRequest.getProfilePicture());

        userRepository.save(existingUser);
    }


    //Всеки път при логин операция, Секюрити ще вика този метод за да ни каже, че някой се опитва да се логне
    //с това потребителско име или каквото решим(имейл, телефон ...)
    //Цел на метода : Да кажа на Spring Security кой е потрбителя зад това потребителско име и той да бъде логнат
    //Return type: Метода очаква да върнем UserDetails обект, който има данните на този потребител
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Username not found."));

        return new UserData(user.getId(), username, user.getPassword(), user.getRole(), user.isActive());
    }

    @Transactional
    @CacheEvict(value = {"users", "usersById", "userDataByUsername"}, allEntries = true)
    public void deleteUser(UUID userId, UUID currentUserId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty() || !userOpt.get().getId().equals(userId)) {
            return;
        }

        if (userId.equals(currentUserId)) {
            throw new IllegalArgumentException("Cannot delete yourself.");
        }

        userRepository.delete(userOpt.get());
        log.info("Admin deleted user [{}]", userOpt.get().getUsername());
    }

    @Cacheable("userDataByUsername")
    public UserData findByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Username not found."));
        return new UserData(user.getId(), username, user.getPassword(), user.getRole(), user.isActive());

    }

    private List<DailyKmDto> leaderboardCache = new ArrayList<>();

    public List<DailyKmDto> getLeaderboard() {
        return new ArrayList<>(leaderboardCache);
    }

    public void recalculateLeaderboard() {
        List<Object[]> results = runRepository.findUsersSortedByTodayKm();
        List<DailyKmDto> temp = new ArrayList<>();

        results.forEach(row -> {
            UUID userId = (UUID) row[0];
            double km = (Double) row[1];
            User user = getById(userId);
            temp.add(new DailyKmDto(userId, user.getUsername(), km));
        });

        leaderboardCache = temp;
    }


    public void resetLeaderboard() {
        leaderboardCache.clear();
    }
}

