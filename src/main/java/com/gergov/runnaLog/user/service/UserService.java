package com.gergov.runnaLog.user.service;

import com.gergov.runnaLog.exception.UserEmailAlreadyExistsException;
import com.gergov.runnaLog.exception.UserNotFoundException;
import com.gergov.runnaLog.exception.UsernameAlreadyExistsException;
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
import com.gergov.runnaLog.web.dto.UserDto;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StatsService statsService;
    private final SubscriptionService subscriptionService;
    private final RunRepository runRepository;
    private final List<DailyKmDto> leaderboardCache;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, StatsService statsService,
                       SubscriptionService subscriptionService, RunRepository runRepository,
                       List<DailyKmDto> leaderboardCache, RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.statsService = statsService;
        this.subscriptionService = subscriptionService;
        this.runRepository = runRepository;
        this.leaderboardCache = leaderboardCache;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    @CacheEvict(value = {"users", "userById"}, allEntries = true)
    public void register(RegisterRequest registerRequest) {

        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException("This username is already taken.");
        }

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new UserEmailAlreadyExistsException("This email is used by another account.");
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

        log.info("New user registered: {}", user.getUsername());
    }


    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Cacheable(value = "userById", key = "#id")
    public UserDto getByIdCached(UUID id) {
        User user = getById(id);
        return toDto(user);
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
    }

    @CacheEvict(value = {"users", "userById"}, allEntries = true)
    public void updateUserProfile(UUID id, EditProfileRequest editProfileRequest) {
        User user = getById(id);
        user.setFirstName(editProfileRequest.getFirstName());
        user.setLastName(editProfileRequest.getLastName());
        user.setProfilePicture(editProfileRequest.getProfilePicture());
        userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = {"users", "userById"}, allEntries = true)
    public void deleteUser(UUID userId, UUID currentUserId) {
        User user = getById(userId);
        if (userId.equals(currentUserId)) {
            throw new IllegalArgumentException("Cannot delete yourself.");
        }
        userRepository.delete(user);
        log.info("Admin deleted user [{}]", user.getUsername());
    }

    public List<DailyKmDto> getLeaderboard() {
        return Collections.unmodifiableList(leaderboardCache);
    }

    public void recalculateLeaderboard() {
        leaderboardCache.clear();
        List<Object[]> results = runRepository.findUsersSortedByTodayKm();
        results.forEach(row -> {
            UUID userId = (UUID) row[0];
            double km = (Double) row[1];
            User user = getById(userId);
            leaderboardCache.add(new DailyKmDto(userId, user.getUsername(), km));
        });
        log.info("Leaderboard recalculated with {} entries", leaderboardCache.size());
    }

    public void resetLeaderboard() {
        leaderboardCache.clear();
        log.info("Leaderboard reset");
    }

    @CacheEvict(value = {"users", "userById"}, allEntries = true)
    public void switchRole(UUID userId) {
        User user = getById(userId);
        user.setRole(user.getRole() == UserRole.USER ? UserRole.ADMIN : UserRole.USER);
        userRepository.save(user);
    }

    public List<User> getAllAdmin() {
        return userRepository.findAllByRole(UserRole.ADMIN);
    }

    @Transactional
    @CacheEvict(value = {"users", "userById"}, allEntries = true)
    public void createAdmin(RegisterRequest registerRequest) {
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.ADMIN)
                .country(registerRequest.getCountry())
                .active(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        userRepository.save(user);
        statsService.createDefaultStats(user);
        subscriptionService.createDefaultSubscription(user);

        log.info("Admin created: {}", user.getUsername());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found."));
        return new UserData(user.getId(), user.getUsername(), user.getPassword(), user.getRole(), user.isActive());
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedOn()
        );
    }

    public List<String> getAllUsernames() {
        Set<String> keys = redisTemplate.keys("userDataByUsername::*");
        return keys.stream()
                .map(k -> k.replace("userDataByUsername::", ""))
                .collect(Collectors.toList());
    }
}
