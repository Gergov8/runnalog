package com.gergov.runnaLog.web;

import com.gergov.runnaLog.run.service.RunService;
import com.gergov.runnaLog.security.UserData;
import com.gergov.runnaLog.stats.model.Stats;
import com.gergov.runnaLog.subscription.model.Subscription;
import com.gergov.runnaLog.subscription.service.SubscriptionService;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.model.UserRole;
import com.gergov.runnaLog.user.service.UserService;
import com.gergov.runnaLog.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
class IndexControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RunService runService;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @Test
    void getIndexPage_ShouldReturnIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void getLoginPage_ShouldReturnLoginViewWithLoginRequest() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    void getRegisterPage_ShouldReturnRegisterViewWithRegisterRequest() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void register_ShouldRedirectToLogin_WhenValid() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "testuser")
                        .param("password", "123456")
                        .param("email", "test@example.com")
                        .param("country", "BULGARIA")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService, times(1)).register(any(RegisterRequest.class));
    }


    @Test
    void getFeedPage_ShouldReturnFeedViewWithModel() throws Exception {
        UUID userId = UUID.randomUUID();
        UserData mockUserData = new UserData(userId, "testuser", "password", UserRole.USER, true);

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername("testuser");
        Stats stats = new Stats();
        mockUser.setStats(stats);

        when(userService.getById(any(UUID.class))).thenReturn(mockUser);
        when(userService.getLeaderboard()).thenReturn(Collections.emptyList());
        when(runService.getFeed(any(User.class))).thenReturn(Collections.emptyList());
        when(subscriptionService.hasActiveEliteSubscription(any(User.class))).thenReturn(true);
        when(subscriptionService.getActiveSubscription(any(User.class))).thenReturn(new Subscription());

        mockMvc.perform(get("/feed").with(user(mockUserData)))
                .andExpect(status().isOk())
                .andExpect(view().name("feed"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("leaderboard"))
                .andExpect(model().attributeExists("stats"))
                .andExpect(model().attributeExists("feedRuns"))
                .andExpect(model().attributeExists("hasEliteSubscription"))
                .andExpect(model().attributeExists("activeSubscription"));
    }
}
