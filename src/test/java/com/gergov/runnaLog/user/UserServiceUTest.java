package com.gergov.runnaLog.user;

import com.gergov.runnaLog.exception.UserEmailAlreadyExistsException;
import com.gergov.runnaLog.exception.UserNotFoundException;
import com.gergov.runnaLog.exception.UsernameAlreadyExistsException;
import com.gergov.runnaLog.run.repository.RunRepository;
import com.gergov.runnaLog.security.UserData;
import com.gergov.runnaLog.stats.service.StatsService;
import com.gergov.runnaLog.subscription.service.SubscriptionService;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.model.UserCountry;
import com.gergov.runnaLog.user.model.UserRole;
import com.gergov.runnaLog.user.repository.UserRepository;
import com.gergov.runnaLog.user.service.UserService;
import com.gergov.runnaLog.web.dto.DailyKmDto;
import com.gergov.runnaLog.web.dto.EditProfileRequest;
import com.gergov.runnaLog.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private StatsService statsService;
    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private RunRepository runRepository;
    @Mock
    private List<DailyKmDto> leaderboardCache;
    @InjectMocks
    private UserService userService;

    @Test
    void register_ShouldCreateUserJohn_WhenDataValid() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john@mail.com");
        request.setPassword("pass123");
        request.setCountry(UserCountry.BULGARIA);

        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@mail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("ENCODED");

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("john")
                .password("ENCODED")
                .email("john@mail.com")
                .country(UserCountry.BULGARIA)
                .role(UserRole.USER)
                .active(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        userService.register(request);

        verify(userRepository).findByUsername("john");
        verify(userRepository).findByEmail("john@mail.com");
        verify(userRepository).save(any(User.class));
        verify(statsService).createDefaultStats(savedUser);
        verify(subscriptionService).createDefaultSubscription(savedUser);
    }

    @Test
    void register_ShouldThrowUsernameAlreadyExists_WhenUsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(new User()));

        assertThrows(UsernameAlreadyExistsException.class,
                () -> userService.register(request));
    }

    @Test
    void register_ShouldThrowUserEmailAlreadyExists_WhenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("mail@mail.com");

        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("mail@mail.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(UserEmailAlreadyExistsException.class,
                () -> userService.register(request));
    }

    @Test
    void getAll_ShouldReturnJohnAndAsen_WhenTheyExist() {
        UUID id = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        User user = new User();
        User user2 = new User();

        user.setId(id);
        user.setUsername("john");

        user2.setId(id2);
        user2.setUsername("asen");

        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        assertEquals(List.of(user, user2), userService.getAll());
    }

    @Test
    void getById_ShouldReturnUser_WhenFound() {
        UUID id = UUID.randomUUID();
        User user = new User();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User result = userService.getById(id);
        assertEquals(user, result);
    }

    @Test
    void getById_ShouldThrowUserNotFound_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getById(id));
    }

    @Test
    void updateUserProfile_ShouldUpdateFields() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);

        EditProfileRequest req = new EditProfileRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setProfilePicture("img.jpg");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.updateUserProfile(id, req);

        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("img.jpg", user.getProfilePicture());
        verify(userRepository).save(user);
    }

    @Test
    void loadUserByUsernameJohn_ShouldReturnUserDataForJohn() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("john");
        user.setPassword("123");
        user.setRole(UserRole.USER);
        user.setActive(true);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        UserData data = (UserData) userService.loadUserByUsername("john");

        assertEquals("john", data.getUsername());
        assertEquals("123", data.getPassword());
        assertEquals(user.getId(), data.getId());
    }

    @Test
    void loadUserByUsername_ShouldThrow_WhenNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.loadUserByUsername("missing"));
    }

    @Test
    void deleteUser_ShouldDelete_WhenValid() {
        UUID id = UUID.randomUUID();
        UUID admin = UUID.randomUUID();

        User user = new User();
        user.setId(id);
        user.setUsername("john");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.deleteUser(id, admin);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_ShouldThrow_WhenTryingToDeleteYourself() {
        UUID same = UUID.randomUUID();
        User user = new User();
        user.setId(same);

        when(userRepository.findById(same)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(same, same));
    }

    @Test
    void switchRole_ShouldSwapBetweenUserAndAdmin() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setRole(UserRole.USER);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.switchRole(id);

        assertEquals(UserRole.ADMIN, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void recalculateLeaderboard_ShouldClearAndRebuild() {

        UUID id = UUID.randomUUID();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        when(runRepository.findUsersSortedByTodayKm(startOfDay, endOfDay))
                .thenReturn(
                        java.util.List.<Object[]>of(new Object[]{id, 10.0})
                );

        User user = new User();
        user.setId(id);
        user.setUsername("john");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.recalculateLeaderboard();

        verify(leaderboardCache).clear();

        verify(leaderboardCache).add(argThat(dto ->
                dto.userId().equals(id)
                        && dto.username().equals("john")
                        && dto.kilometers() == 10.0
        ));
    }

    @Test
    void createAdmin_ShouldCreateAdminUserAndInitializeServices() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("Admin");
        request.setEmail("admin1@gmail.com");
        request.setPassword("admin1");
        request.setCountry(UserCountry.BULGARIA);

        when(passwordEncoder.encode("admin1")).thenReturn("encodedPass");

        userService.createAdmin(request);

        verify(passwordEncoder).encode("admin1");

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("Admin") &&
                        user.getEmail().equals("admin1@gmail.com") &&
                        user.getPassword().equals("encodedPass") &&
                        user.getRole() == UserRole.ADMIN &&
                        user.isActive() &&
                        user.getCountry().equals(UserCountry.BULGARIA) &&
                        user.getCreatedOn() != null &&
                        user.getUpdatedOn() != null
        ));

        verify(statsService).createDefaultStats(any(User.class));
        verify(subscriptionService).createDefaultSubscription(any(User.class));
    }
}
