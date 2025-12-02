package com.gergov.runnaLog.trainingPlan;

import com.gergov.runnaLog.feign.PlanClient;
import com.gergov.runnaLog.feign.dto.CreatePlanRequest;
import com.gergov.runnaLog.feign.dto.CreatePlanResponse;
import com.gergov.runnaLog.feign.dto.FeignPlanRequest;
import com.gergov.runnaLog.feign.dto.Plan;
import com.gergov.runnaLog.subscription.model.SubscriptionType;
import com.gergov.runnaLog.trainingPlan.service.TrainingPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TrainingPlanUTest {

    @Mock
    private PlanClient planClient;

    private TrainingPlanService trainingPlanService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        trainingPlanService = new TrainingPlanService(planClient);
    }

    @Test
    void testConnection_ShouldReturnResponse() {
        when(planClient.sayHello()).thenReturn("Hello!");

        String response = trainingPlanService.testConnection();

        assertThat(response).isEqualTo("Hello!");
        verify(planClient).sayHello();
    }

    @Test
    void testConnection_ShouldHandleException() {
        when(planClient.sayHello()).thenThrow(new RuntimeException("Service down"));

        String response = trainingPlanService.testConnection();

        assertThat(response).startsWith("Connection failed");
        verify(planClient).sayHello();
    }

    @Test
    void createTrainingPlan_ShouldCallClient() {
        CreatePlanRequest request = new CreatePlanRequest();
        request.setUserId(UUID.randomUUID());
        request.setDistanceKm(10.0);
        request.setPlanLevel(SubscriptionType.RECREATIONAL);
        request.setDaysPerWeek(6);

        CreatePlanResponse mockResponse = new CreatePlanResponse();
        when(planClient.createPlan(any(FeignPlanRequest.class))).thenReturn(mockResponse);

        CreatePlanResponse response = trainingPlanService.createTrainingPlan(request);

        assertThat(response).isEqualTo(mockResponse);

        ArgumentCaptor<FeignPlanRequest> captor = ArgumentCaptor.forClass(FeignPlanRequest.class);
        verify(planClient).createPlan(captor.capture());

        FeignPlanRequest feignRequest = captor.getValue();
        assertThat(feignRequest.getUserId()).isEqualTo(request.getUserId());
        assertThat(feignRequest.getDistanceKm()).isEqualTo(request.getDistanceKm());
        assertThat(feignRequest.getDaysPerWeek()).isEqualTo(request.getDaysPerWeek());
        assertThat(feignRequest.getPlanLevel()).isNotNull();
    }

    @Test
    void getPlan_ShouldReturnPlan() {
        UUID planId = UUID.randomUUID();
        Plan plan = new Plan();
        when(planClient.getPlan(planId)).thenReturn(plan);

        Optional<Plan> result = trainingPlanService.getPlan(planId);

        assertThat(result).isPresent().contains(plan);
        verify(planClient).getPlan(planId);
    }

    @Test
    void getPlan_ShouldReturnEmptyOnException() {
        UUID planId = UUID.randomUUID();
        when(planClient.getPlan(planId)).thenThrow(new RuntimeException("Not found"));

        Optional<Plan> result = trainingPlanService.getPlan(planId);

        assertThat(result).isEmpty();
        verify(planClient).getPlan(planId);
    }

    @Test
    void deletePlan_ShouldCallClient() {
        UUID planId = UUID.randomUUID();

        trainingPlanService.deletePlan(planId);

        verify(planClient).deletePlan(planId);
    }

    @Test
    void deletePlan_ShouldThrowRuntimeExceptionOnFailure() {
        UUID planId = UUID.randomUUID();
        doThrow(new RuntimeException("Delete failed")).when(planClient).deletePlan(planId);

        try {
            trainingPlanService.deletePlan(planId);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Failed to delete training plan");
        }

        verify(planClient).deletePlan(planId);
    }

    @Test
    void getUserPlans_ShouldReturnList() {
        UUID userId = UUID.randomUUID();
        Plan plan = new Plan();
        when(planClient.getUserPlans(userId)).thenReturn(List.of(plan));

        List<Plan> plans = trainingPlanService.getUserPlans(userId);

        assertThat(plans).hasSize(1).contains(plan);
        verify(planClient).getUserPlans(userId);
    }

    @Test
    void getUserPlans_ShouldReturnEmptyOnException() {
        UUID userId = UUID.randomUUID();
        when(planClient.getUserPlans(userId)).thenThrow(new RuntimeException("Error"));

        List<Plan> plans = trainingPlanService.getUserPlans(userId);

        assertThat(plans).isEmpty();
        verify(planClient).getUserPlans(userId);
    }

    @Test
    void getUserPlanCount_ShouldReturnCount() {
        UUID userId = UUID.randomUUID();
        when(planClient.getUserPlanCount(userId)).thenReturn(5L);

        long count = trainingPlanService.getUserPlanCount(userId);

        assertThat(count).isEqualTo(5L);
        verify(planClient).getUserPlanCount(userId);
    }

    @Test
    void getUserPlanCount_ShouldReturnZeroOnException() {
        UUID userId = UUID.randomUUID();
        when(planClient.getUserPlanCount(userId)).thenThrow(new RuntimeException("Error"));

        long count = trainingPlanService.getUserPlanCount(userId);

        assertThat(count).isZero();
        verify(planClient).getUserPlanCount(userId);
    }
}
