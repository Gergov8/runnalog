package com.gergov.runnaLog.web;

import com.gergov.runnaLog.feign.dto.Plan;
import com.gergov.runnaLog.security.UserData;
import com.gergov.runnaLog.trainingPlan.service.TrainingPlanService;
import com.gergov.runnaLog.feign.dto.CreatePlanRequest;
import com.gergov.runnaLog.feign.dto.CreatePlanResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {
    private final TrainingPlanService planService;

    @Autowired
    public PlanController(TrainingPlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    public ResponseEntity<CreatePlanResponse> createPlan(
            @AuthenticationPrincipal UserData userData,
            @RequestBody CreatePlanRequest request) {

        CreatePlanResponse response = planService.createTrainingPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        planService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Plan> getPlan(@PathVariable UUID id) {
        return planService.getPlan(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/say-hello")
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello from PlanController");
    }
}