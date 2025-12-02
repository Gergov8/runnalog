package com.gergov.runnaLog.user;

import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.model.UserCountry;
import com.gergov.runnaLog.user.model.UserRole;
import com.gergov.runnaLog.user.property.UserProperties;
import com.gergov.runnaLog.user.service.UserInit;
import com.gergov.runnaLog.user.service.UserService;
import com.gergov.runnaLog.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UserInitUTest {

    private UserService userService;
    private UserProperties userProperties;
    private UserInit userInit;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        userProperties = mock(UserProperties.class);
        userInit = new UserInit(userService, userProperties);
    }

    @Test
    void run_ShouldCreateDefaultAdmin_WhenNoAdminExists() throws Exception {
        when(userService.getAllAdmin()).thenReturn(Collections.emptyList());

        UserProperties.DefaultUser defaultUser = mock(UserProperties.DefaultUser.class);
        when(defaultUser.getUsername()).thenReturn("admin");
        when(defaultUser.getEmail()).thenReturn("admin@example.com");
        when(defaultUser.getPassword()).thenReturn("password123");
        when(defaultUser.getCountry()).thenReturn(UserCountry.BULGARIA);
        when(userProperties.getDefaultUser()).thenReturn(defaultUser);

        userInit.run(mock(ApplicationArguments.class));

        ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);
        verify(userService, times(1)).createAdmin(captor.capture());

        RegisterRequest request = captor.getValue();
        assertThat(request.getUsername()).isEqualTo("admin");
        assertThat(request.getEmail()).isEqualTo("admin@example.com");
        assertThat(request.getPassword()).isEqualTo("password123");
        assertThat(request.getCountry()).isEqualTo(UserCountry.BULGARIA);
    }

    @Test
    void run_ShouldNotCreateAdmin_WhenAdminAlreadyExists() throws Exception {
        User existingAdmin = new User();
        existingAdmin.setRole(UserRole.ADMIN);

        when(userService.getAllAdmin()).thenReturn(List.of(existingAdmin));

        userInit.run(mock(ApplicationArguments.class));

        verify(userService, never()).createAdmin(any());
    }
}
