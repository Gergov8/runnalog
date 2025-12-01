package com.gergov.runnaLog.web;

import com.gergov.runnaLog.security.UserData;
import com.gergov.runnaLog.stats.model.Stats;
import com.gergov.runnaLog.subscription.model.Subscription;
import com.gergov.runnaLog.subscription.model.SubscriptionType;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.model.UserRole;
import com.gergov.runnaLog.user.service.UserService;
import com.gergov.runnaLog.web.dto.EditProfileRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void getProfilePage_ShouldReturnProfileView() throws Exception {
        UUID userId = UUID.randomUUID();
        UserData userData = new UserData(userId, "testuser", "password", UserRole.USER, true);

        Subscription subscription = new Subscription();
        subscription.setType(SubscriptionType.RECREATIONAL);

        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setStats(new Stats());
        user.setRuns(Collections.emptyList());
        user.setSubscriptions(List.of(subscription));

        when(userService.getById(userId)).thenReturn(user);

        mockMvc.perform(get("/users/profile").with(user(userData)))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("stats"))
                .andExpect(model().attributeExists("runs"))
                .andExpect(model().attributeExists("subs"))
                .andExpect(model().attributeExists("date"));
    }

    @Test
    void getProfileEditPage_ShouldReturnEditProfileView() throws Exception {
        UUID userId = UUID.randomUUID();
        UserData userData = new UserData(userId, "testuser", "password", UserRole.USER, true);

        User user = new User();
        user.setId(userId);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setProfilePicture("pic.png");

        when(userService.getById(userId)).thenReturn(user);

        mockMvc.perform(get("/users/profile/edit").with(user(userData)))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("editProfileRequest"));
    }

    @Test
    void updateProfile_ShouldRedirectToProfile_WhenValid() throws Exception {
        UUID userId = UUID.randomUUID();
        UserData userData = new UserData(userId, "testuser", "password", UserRole.USER, true);

        User user = new User();
        user.setId(userId);

        when(userService.getById(userId)).thenReturn(user);

        mockMvc.perform(post("/users/profile")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("profilePicture", "pic.png")
                        .with(user(userData))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/profile"));

        verify(userService, times(1)).updateUserProfile(eq(userId), any(EditProfileRequest.class));
    }

    @Test
    void getUsers_ShouldReturnUsersView_ForAdmin() throws Exception {
        User adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setRole(UserRole.ADMIN);

        when(userService.getAll()).thenReturn(Collections.singletonList(adminUser));

        mockMvc.perform(get("/users").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    void deleteUser_ShouldRedirectToUsers_ForAdmin() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UserData adminData = new UserData(adminId, "admin", "password", UserRole.ADMIN, true);

        doNothing().when(userService).deleteUser(userId, adminId);

        mockMvc.perform(delete("/users/delete/" + userId).with(user(adminData)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        verify(userService, times(1)).deleteUser(userId, adminId);
    }

    @Test
    void switchUserRole_ShouldRedirectToUsers_ForAdmin() throws Exception {
        UUID userId = UUID.randomUUID();

        doNothing().when(userService).switchRole(userId);

        mockMvc.perform(put("/users/role/" + userId).with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        verify(userService, times(1)).switchRole(userId);
    }
}

