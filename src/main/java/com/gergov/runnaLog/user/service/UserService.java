package com.gergov.runnaLog.user.service;

import com.gergov.runnaLog.security.UserData;
import com.gergov.runnaLog.stats.service.StatsService;
import com.gergov.runnaLog.subscription.service.SubscriptionService;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.model.UserRole;
import com.gergov.runnaLog.user.repository.UserRepository;
import com.gergov.runnaLog.web.dto.EditProfileRequest;
import com.gergov.runnaLog.web.dto.LoginRequest;
import com.gergov.runnaLog.web.dto.RegisterRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, StatsService statsService, SubscriptionService subscriptionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.statsService = statsService;
        this.subscriptionService = subscriptionService;
    }

    public User login(LoginRequest loginRequest) {

        Optional<User> optionalUser = userRepository.findByUsername(loginRequest.getUsername());
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Incorrect username or password.");
        }

        String rawPassword = loginRequest.getPassword();
        String hashedPassword = optionalUser.get().getPassword();

        if (!passwordEncoder.matches(rawPassword, hashedPassword)) {
            throw new RuntimeException("Incorrect username or password.");
        }

        return optionalUser.get();
    }

    @Transactional
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

        log.info("New user profile was registered in the system for user [%s].".formatted(registerRequest.getUsername()));
    }

    public List<User> getAll() {

        return userRepository.findAll();
    }

    public User getByUsername(String username) {

        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User with [%s] username not found.".formatted(username)));
    }

    public User getById(UUID id) {

        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User with [%s] id does not exist.".formatted(id)));
    }

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

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

}
