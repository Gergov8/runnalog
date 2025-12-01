package com.gergov.runnaLog.subscription;

import com.gergov.runnaLog.stats.model.Stats;
import com.gergov.runnaLog.stats.repository.StatsRepository;
import com.gergov.runnaLog.subscription.model.*;
import com.gergov.runnaLog.subscription.repository.SubscriptionRepository;
import com.gergov.runnaLog.subscription.service.SubscriptionService;
import com.gergov.runnaLog.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceUTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private StatsRepository statsRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    void createDefaultSubscription_ShouldSaveSubscription() {
        User user = new User();

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);

        subscriptionService.createDefaultSubscription(user);

        verify(subscriptionRepository).save(captor.capture());
        Subscription saved = captor.getValue();

        assertEquals(user, saved.getUser());
        assertEquals(SubscriptionStatus.ACTIVE, saved.getStatus());
        assertEquals(SubscriptionPeriod.MONTHLY, saved.getPeriod());
        assertEquals(SubscriptionType.RECREATIONAL, saved.getType());
        assertEquals(0, saved.getPrice());
        assertTrue(saved.isRenewalAllowed());
        assertNotNull(saved.getCreatedOn());
        assertTrue(saved.getExpiryOn().isAfter(LocalDateTime.now()));
    }

    @Test
    void purchaseSubscription_ShouldReturnFalse_WhenNotEnoughStrides() {
        User user = new User();
        Stats stats = new Stats();
        stats.setStrides(1000);
        user.setStats(stats);

        Subscription existing = new Subscription();
        when(subscriptionRepository.findLatestByUser(user)).thenReturn(existing);

        boolean result = subscriptionService.purchaseSubscription(user, SubscriptionType.ELITE);

        assertFalse(result);
        verify(statsRepository, never()).save(any());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void purchaseSubscription_ShouldDeductStridesAndSaveSubscription_WhenEnoughStrides() {
        User user = new User();
        Stats stats = new Stats();
        stats.setStrides(20000);
        user.setStats(stats);

        Subscription existing = new Subscription();
        existing.setStatus(SubscriptionStatus.ACTIVE);

        when(subscriptionRepository.findLatestByUser(user)).thenReturn(existing);

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);

        boolean result = subscriptionService.purchaseSubscription(user, SubscriptionType.COMPETITIVE);

        assertTrue(result);
        assertEquals(14000, user.getStats().getStrides());

        verify(statsRepository).save(stats);
        verify(subscriptionRepository).save(captor.capture());

        Subscription saved = captor.getValue();
        assertEquals(user, saved.getUser());
        assertEquals(SubscriptionType.COMPETITIVE, saved.getType());
        assertEquals(6000, saved.getPrice());
        assertEquals(SubscriptionStatus.ACTIVE, saved.getStatus());

        assertEquals(SubscriptionStatus.TERMINATED, existing.getStatus());
    }

    @Test
    void hasActiveEliteSubscription_ShouldReturnFalse_WhenUserNull() {
        assertFalse(subscriptionService.hasActiveEliteSubscription(null));
    }

    @Test
    void hasActiveEliteSubscription_ShouldReturnTrue_WhenEliteAndActive() {
        User user = new User();
        Subscription sub = new Subscription();
        sub.setType(SubscriptionType.ELITE);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setExpiryOn(LocalDateTime.now().plusDays(10));

        when(subscriptionRepository.findLatestActiveByUser(user)).thenReturn(sub);

        assertTrue(subscriptionService.hasActiveEliteSubscription(user));
    }

    @Test
    void hasActiveEliteSubscription_ShouldReturnFalse_WhenNotElite() {
        User user = new User();
        Subscription sub = new Subscription();
        sub.setType(SubscriptionType.COMPETITIVE);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setExpiryOn(LocalDateTime.now().plusDays(10));

        when(subscriptionRepository.findLatestActiveByUser(user)).thenReturn(sub);

        assertFalse(subscriptionService.hasActiveEliteSubscription(user));
    }

    @Test
    void hasActiveEliteSubscription_ShouldReturnFalse_WhenExpired() {
        User user = new User();
        Subscription sub = new Subscription();
        sub.setType(SubscriptionType.ELITE);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setExpiryOn(LocalDateTime.now().minusDays(1));

        when(subscriptionRepository.findLatestActiveByUser(user)).thenReturn(sub);

        assertFalse(subscriptionService.hasActiveEliteSubscription(user));
    }

    @Test
    void getActiveSubscription_ShouldReturnSubscription() {
        User user = new User();
        Subscription sub = new Subscription();

        when(subscriptionRepository.findLatestActiveByUser(user)).thenReturn(sub);

        assertEquals(sub, subscriptionService.getActiveSubscription(user));
    }

    @Test
    void getActiveSubscription_ShouldReturnNull_WhenUserNull() {
        assertNull(subscriptionService.getActiveSubscription(null));
    }
}

