package com.gergov.runnaLog.web;

import com.gergov.runnaLog.security.UserData;
import com.gergov.runnaLog.stats.model.Stats;
import com.gergov.runnaLog.subscription.model.SubscriptionType;
import com.gergov.runnaLog.subscription.service.SubscriptionService;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.model.UserRole;
import com.gergov.runnaLog.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubscriptionController.class)
class SubscriptionControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @Test
    void getUpgradePage_ShouldReturnSubscriptionsView() throws Exception {
        User user = new User();
        Stats stats = new Stats();
        user.setStats(stats);

        UserData userData = new UserData(UUID.randomUUID(), "testuser", "pass", UserRole.USER, true);
        when(userService.getById(userData.getId())).thenReturn(user);

        mockMvc.perform(get("/subscriptions").with(user(userData)))
                .andExpect(status().isOk())
                .andExpect(view().name("subscriptions"))
                .andExpect(model().attributeExists("subscriptionUpgradeRequest"))
                .andExpect(model().attributeExists("stats"));

        verify(userService).getById(userData.getId());
    }

    @Test
    void getSubscriptionHistoryPage_ShouldReturnHistoryView() throws Exception {
        User user = new User();
        UserData userData = new UserData(UUID.randomUUID(), "testuser", "pass", UserRole.USER, true);
        when(userService.getById(userData.getId())).thenReturn(user);

        mockMvc.perform(get("/subscriptions/history").with(user(userData)))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription-history"))
                .andExpect(model().attributeExists("user"));

        verify(userService).getById(userData.getId());
    }

    @Test
    void purchaseSubscription_ShouldRedirectToSubscriptions_WhenSuccessful() throws Exception {
        User user = new User();
        Stats stats = new Stats();
        stats.setStrides(16000);
        user.setStats(stats);

        UserData userData = new UserData(UUID.randomUUID(), "testuser", "pass", UserRole.USER, true);
        when(userService.getById(userData.getId())).thenReturn(user);
        when(subscriptionService.purchaseSubscription(user, SubscriptionType.ELITE)).thenReturn(true);

        mockMvc.perform(post("/subscriptions/purchase")
                        .param("type", String.valueOf(SubscriptionType.ELITE))
                        .with(user(userData))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/subscriptions?success"));

        verify(subscriptionService).purchaseSubscription(user, SubscriptionType.ELITE);
    }

    @Test
    void purchaseSubscription_ShouldRedirectWithError_WhenNotEnoughSTR() throws Exception {
        User user = new User();
        Stats stats = new Stats();
        stats.setStrides(500);
        user.setStats(stats);

        UserData userData = new UserData(UUID.randomUUID(), "testuser", "pass", UserRole.USER, true);
        when(userService.getById(userData.getId())).thenReturn(user);
        when(subscriptionService.purchaseSubscription(user, SubscriptionType.ELITE)).thenReturn(false);

        mockMvc.perform(post("/subscriptions/purchase")
                        .param("type", String.valueOf(SubscriptionType.ELITE))
                        .with(user(userData))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/subscriptions?error=Not enough STR to purchase this plan."));

        verify(subscriptionService).purchaseSubscription(user, SubscriptionType.ELITE);
    }

    @Test
    void purchaseSubscription_ShouldRedirectWithError_WhenBindingResultHasErrors() throws Exception {
        User user = new User();
        Stats stats = new Stats();
        user.setStats(stats);

        UserData userData = new UserData(UUID.randomUUID(), "testuser", "pass", UserRole.USER, true);
        when(userService.getById(userData.getId())).thenReturn(user);

        mockMvc.perform(post("/subscriptions/purchase")
                        .param("type", "")
                        .with(user(userData))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/subscriptions?error=Invalid subscription data. Please try again."));
    }
}
