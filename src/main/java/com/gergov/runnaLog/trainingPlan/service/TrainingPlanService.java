package com.gergov.runnaLog.trainingPlan.service;

import com.gergov.runnaLog.feign.PlanClient;
import com.gergov.runnaLog.feign.dto.FeignPlanRequest;
import com.gergov.runnaLog.feign.dto.Plan;
import com.gergov.runnaLog.trainingPlan.util.PlanLevelConverter;
import com.gergov.runnaLog.feign.dto.CreatePlanRequest;
import com.gergov.runnaLog.feign.dto.CreatePlanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TrainingPlanService {

    private static final Logger log = LoggerFactory.getLogger(TrainingPlanService.class);
    private final PlanClient planClient;

    @Autowired
    public TrainingPlanService(PlanClient planClient) {
        this.planClient = planClient;
    }

    public String testConnection() {
        try {
            String response = planClient.sayHello();
            log.info("Microservice connection test: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Failed to connect to training microservice", e);
            return "Connection failed: " + e.getMessage();
        }
    }

    public CreatePlanResponse createTrainingPlan(CreatePlanRequest request) {

        log.info("Sending request to microservice: {}", request);

        FeignPlanRequest feignRequest = new FeignPlanRequest(
                request.getUserId(),
                request.getDistanceKm(),
                PlanLevelConverter.toPlanLevel(request.getPlanLevel()),
                request.getDaysPerWeek()
        );

        return planClient.createPlan(feignRequest);
    }

    public Optional<Plan> getPlan(UUID planId) {
        try {
            Plan plan = planClient.getPlan(planId);
            log.info("Retrieved training plan for ID: {}", planId);
            return Optional.of(plan);
        } catch (Exception e) {
            log.error("Failed to get training plan with ID: {}", planId, e);
            return Optional.empty();
        }
    }

    public void deletePlan(UUID planId) {
        try {
            planClient.deletePlan(planId);
            log.info("Deleted training plan with ID: {}", planId);
        } catch (Exception e) {
            log.error("Failed to delete training plan with ID: {}", planId, e);
            throw new RuntimeException("Failed to delete training plan: " + e.getMessage(), e);
        }
    }

    public List<Plan> getUserPlans(UUID userId) {
        try {
            return planClient.getUserPlans(userId);
        } catch (Exception e) {
            log.error("Failed to get plans for user: {}", userId, e);
            return Collections.emptyList();
        }
    }

    public long getUserPlanCount(UUID userId) {
        try {
            return planClient.getUserPlanCount(userId);
        } catch (Exception e) {
            log.error("Failed to get plan count for user: {}", userId, e);
            return 0;
        }

    }
}