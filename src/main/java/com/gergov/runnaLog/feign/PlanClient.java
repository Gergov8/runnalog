package com.gergov.runnaLog.feign;

import com.gergov.runnaLog.feign.dto.FeignPlanRequest;
import com.gergov.runnaLog.feign.dto.CreatePlanResponse;
import com.gergov.runnaLog.feign.dto.Plan;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "trainingPlan-svc", url = "http://localhost:8081")
public interface PlanClient {

    @PostMapping("/api/v1/plans")
    CreatePlanResponse createPlan(@RequestBody FeignPlanRequest request);

    @PutMapping("/api/v1/plans/{id}/regenerate")
    CreatePlanResponse regeneratePlan(@PathVariable("id") UUID id, @RequestBody FeignPlanRequest request);

    @DeleteMapping("/api/v1/plans/{id}")
    void deletePlan(@PathVariable("id") UUID id);

    @GetMapping("/api/v1/plans/{id}")
    Plan getPlan(@PathVariable("id") UUID id);

    @GetMapping("/api/v1/plans/user/{userId}")
    List<Plan> getUserPlans(@PathVariable("userId") UUID userId);

    @GetMapping("/api/v1/plans/user/{userId}/count")
    Long getUserPlanCount(@PathVariable("userId") UUID userId);

    @DeleteMapping("/api/v1/plans/user/{userId}")
    void deleteUserPlans(@PathVariable("userId") UUID userId);

    @GetMapping("/api/v1/plans/say-hello")
    String sayHello();
}