package com.gergov.runnaLog.user;

import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.model.RunVisibility;
import com.gergov.runnaLog.run.service.RunService;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.model.UserCountry;
import com.gergov.runnaLog.user.model.UserRole;
import com.gergov.runnaLog.user.property.UserProperties;
import com.gergov.runnaLog.user.service.UserInit;
import com.gergov.runnaLog.user.service.UserService;
import com.gergov.runnaLog.web.dto.CreateRunRequest;
import com.gergov.runnaLog.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

@ExtendWith(MockitoExtension.class)
public class UserInitUTest {

    private UserService userService;
    private RunService runService;
    private UserInit userInit;
    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setUp() {

        userService = mock(UserService.class);
        UserProperties userProperties = mock(UserProperties.class);
        runService = mock(RunService.class);

        userInit = new UserInit(userService, userProperties, runService);

        UserProperties.DefaultUser defaultUser = new UserProperties.DefaultUser();
        defaultUser.setUsername("admin");
        defaultUser.setEmail("admin@example.com");
        defaultUser.setPassword("password123");
        defaultUser.setCountry(UserCountry.BULGARIA);

        UserProperties.DefaultUser2 defaultUser2 = new UserProperties.DefaultUser2();
        defaultUser2.setUsername("john");
        defaultUser2.setEmail("john@example.com");
        defaultUser2.setPassword("john123");
        defaultUser2.setCountry(UserCountry.BULGARIA);

        when(userProperties.getDefaultUser()).thenReturn(defaultUser);
        when(userProperties.getDefaultUser2()).thenReturn(defaultUser2);

        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setRole(UserRole.ADMIN);

        normalUser = new User();
        normalUser.setUsername("john");
        normalUser.setRole(UserRole.USER);
    }

    @Test
    void run_ShouldCreateDefaultAdmin_WhenNoAdminExists() throws Exception {

        when(userService.getAllAdmin()).thenReturn(Collections.emptyList());
        when(userService.getAll()).thenReturn(List.of()); // No normal users

        when(userService.getByUsername("admin")).thenReturn(adminUser);
        when(userService.getByUsername("john")).thenReturn(normalUser);

        when(runService.getRunsByUser(adminUser)).thenReturn(List.of());
        when(runService.getRunsByUser(normalUser)).thenReturn(List.of());

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
    void run_ShouldNotCreateAdmin_WhenAdminExists() throws Exception {

        when(userService.getAllAdmin()).thenReturn(List.of(adminUser));
        when(userService.getAll()).thenReturn(List.of(normalUser));

        when(userService.getByUsername("admin")).thenReturn(adminUser);
        when(userService.getByUsername("john")).thenReturn(normalUser);

        when(runService.getRunsByUser(any())).thenReturn(List.of(new Run()));

        userInit.run(mock(ApplicationArguments.class));

        verify(userService, never()).createAdmin(any());
    }

    @Test
    void run_ShouldCreateDefaultUser_WhenNoneExists() throws Exception {

        when(userService.getAllAdmin()).thenReturn(List.of(adminUser));
        when(userService.getAll()).thenReturn(List.of(adminUser)); // No USER role

        when(userService.getByUsername("admin")).thenReturn(adminUser);
        when(userService.getByUsername("john")).thenReturn(normalUser);

        when(runService.getRunsByUser(adminUser)).thenReturn(List.of(new Run()));
        when(runService.getRunsByUser(normalUser)).thenReturn(List.of());

        userInit.run(mock(ApplicationArguments.class));

        ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);
        verify(userService, times(1)).createDefaultUser(captor.capture());

        RegisterRequest req = captor.getValue();
        assertThat(req.getUsername()).isEqualTo("john");
        assertThat(req.getEmail()).isEqualTo("john@example.com");
        assertThat(req.getPassword()).isEqualTo("john123");
        assertThat(req.getCountry()).isEqualTo(UserCountry.BULGARIA);
    }

    @Test
    void run_ShouldNotCreateDefaultUser_WhenUserExists() throws Exception {

        when(userService.getAllAdmin()).thenReturn(List.of(adminUser));
        when(userService.getAll()).thenReturn(List.of(adminUser, normalUser));

        when(userService.getByUsername("admin")).thenReturn(adminUser);
        when(userService.getByUsername("john")).thenReturn(normalUser);

        when(runService.getRunsByUser(any())).thenReturn(List.of(new Run()));

        userInit.run(mock(ApplicationArguments.class));

        verify(userService, never()).createDefaultUser(any());
    }

    @Test
    void run_ShouldCreateDefaultRunForAdmin_WhenNoRunsExist() throws Exception {

        when(userService.getAllAdmin()).thenReturn(List.of(adminUser));
        when(userService.getAll()).thenReturn(List.of(adminUser, normalUser));

        when(userService.getByUsername("admin")).thenReturn(adminUser);
        when(runService.getRunsByUser(adminUser)).thenReturn(List.of()); // No runs

        when(userService.getByUsername("john")).thenReturn(normalUser);
        when(runService.getRunsByUser(normalUser)).thenReturn(List.of(new Run()));

        userInit.run(mock(ApplicationArguments.class));

        ArgumentCaptor<CreateRunRequest> captor = ArgumentCaptor.forClass(CreateRunRequest.class);
        verify(runService, times(1)).createRun(captor.capture(), eq(adminUser));

        CreateRunRequest req = captor.getValue();
        assertThat(req.getDistance()).isEqualTo(10.0);
        assertThat(req.getVisibility()).isEqualTo(RunVisibility.PUBLIC);
    }

    @Test
    void run_ShouldNotCreateDefaultRunForAdmin_WhenRunsExist() throws Exception {

        when(userService.getAllAdmin()).thenReturn(List.of(adminUser));
        when(userService.getAll()).thenReturn(List.of(adminUser, normalUser));

        when(userService.getByUsername("admin")).thenReturn(adminUser);
        when(runService.getRunsByUser(adminUser)).thenReturn(List.of(new Run()));

        when(userService.getByUsername("john")).thenReturn(normalUser);
        when(runService.getRunsByUser(normalUser)).thenReturn(List.of(new Run()));

        userInit.run(mock(ApplicationArguments.class));

        verify(runService, never()).createRun(any(), eq(adminUser));
    }

    @Test
    void run_ShouldCreateDefaultRunForUser_WhenNoRunsExist() throws Exception {

        when(userService.getAllAdmin()).thenReturn(List.of(adminUser));
        when(userService.getAll()).thenReturn(List.of(adminUser, normalUser));

        when(userService.getByUsername("admin")).thenReturn(adminUser);
        when(runService.getRunsByUser(adminUser)).thenReturn(List.of(new Run()));

        when(userService.getByUsername("john")).thenReturn(normalUser);
        when(runService.getRunsByUser(normalUser)).thenReturn(List.of());

        userInit.run(mock(ApplicationArguments.class));

        ArgumentCaptor<CreateRunRequest> captor = ArgumentCaptor.forClass(CreateRunRequest.class);
        verify(runService, times(1)).createRun(captor.capture(), eq(normalUser));

        CreateRunRequest req = captor.getValue();
        assertThat(req.getDistance()).isEqualTo(5.0);
        assertThat(req.getVisibility()).isEqualTo(RunVisibility.PUBLIC);
    }

    @Test
    void run_ShouldNotCreateDefaultRunForUser_WhenRunsExist() throws Exception {

        when(userService.getAllAdmin()).thenReturn(List.of(adminUser));
        when(userService.getAll()).thenReturn(List.of(adminUser, normalUser));

        when(userService.getByUsername(anyString())).thenReturn(adminUser);
        when(runService.getRunsByUser(any())).thenReturn(List.of(new Run()));

        userInit.run(mock(ApplicationArguments.class));

        verify(runService, never()).createRun(any(), eq(normalUser));
    }
}
