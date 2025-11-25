package com.gergov.runnaLog.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gergov.runnaLog.feign.dto.Plan;
import com.gergov.runnaLog.subscription.service.SubscriptionService;
import com.gergov.runnaLog.trainingPlan.service.TrainingPlanService;
import com.gergov.runnaLog.security.UserData;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.service.UserService;
import com.gergov.runnaLog.feign.dto.CreatePlanRequest;
import com.gergov.runnaLog.feign.dto.CreatePlanResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/training")
public class PlanViewController {

    private final UserService userService;
    private final TrainingPlanService trainingPlanService;
    private final SubscriptionService subscriptionService;


    @Autowired
    public PlanViewController(UserService userService, TrainingPlanService trainingPlanService, SubscriptionService subscriptionService) {
        this.userService = userService;
        this.trainingPlanService = trainingPlanService;
        this.subscriptionService = subscriptionService;
    }

        @GetMapping("/plans")
        @PreAuthorize("hasRole('ADMIN') or @subscriptionService.hasActiveEliteSubscription(authentication.principal)")
        public String trainingPlansDashboard(
                Model model,
                @AuthenticationPrincipal UserData userData,
                @RequestParam(value = "message", required = false) String message,
                @RequestParam(value = "error", required = false) String error,
                @RequestParam(value = "connectionStatus", required = false) String connectionStatus,
                @RequestParam(value = "planId", required = false) String planId) {

            User user = userService.getById(userData.getId());
            model.addAttribute("user", user);
            model.addAttribute("pageTitle", "Training Plans Dashboard");
            model.addAttribute("createPlanRequest", new CreatePlanRequest());
            model.addAttribute("userPlans", trainingPlanService.getUserPlans(user.getId()));
            model.addAttribute("plansCount", trainingPlanService.getUserPlanCount(user.getId()));

            if (message != null) model.addAttribute("message", message);
            if (error != null) model.addAttribute("error", error);
            if (connectionStatus != null) model.addAttribute("connectionStatus", connectionStatus);
            model.addAttribute("planId", planId != null ? planId : "");

            return "training-plans";
        }

    @PostMapping("/create")
    public String createTrainingPlan(
            @ModelAttribute CreatePlanRequest createPlanRequest,
            @AuthenticationPrincipal UserData userData,
            RedirectAttributes redirectAttributes) {

        try {
            createPlanRequest.setUserId(userData.getId());
            CreatePlanResponse response = trainingPlanService.createTrainingPlan(createPlanRequest);

            // Only pass planId to make URLs consistent
            return "redirect:/training/plan-result?planId=" + response.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create training plan: " + e.getMessage());
            return "redirect:/training/plans";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteTrainingPlan(
            @PathVariable UUID id,
            RedirectAttributes redirectAttributes) {

        try {
            trainingPlanService.deletePlan(id);
            redirectAttributes.addFlashAttribute("message", "Training plan deleted successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete training plan: " + e.getMessage());
        }

        return "redirect:/training/plans";
    }

    @GetMapping("/plan-result")
    public String showTrainingPlanResult(
            Model model,
            @AuthenticationPrincipal UserData userData,
            @RequestParam String planId,
            @RequestParam(required = false) Double distanceKm,
            @RequestParam(required = false) String planLevel,
            @RequestParam(required = false) Integer daysPerWeek) {

        User user = userService.getById(userData.getId());
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Your Training Plan");
        model.addAttribute("planId", planId);

        // Try to fetch the full plan from microservice
        Optional<Plan> planOptional = trainingPlanService.getPlan(UUID.fromString(planId));

        if (planOptional.isPresent()) {
            Plan plan = planOptional.get();
            model.addAttribute("plan", plan);

            // Check if plan JSON is actually populated
            if (plan.getPlanJson() != null && !plan.getPlanJson().trim().isEmpty()) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode planJson = objectMapper.readTree(plan.getPlanJson());
                    model.addAttribute("planJson", planJson);
                    model.addAttribute("planReady", true);
                } catch (Exception e) {
                    model.addAttribute("error", "Failed to parse training plan JSON");
                }
            } else {
                // Plan exists but JSON is not ready yet
                model.addAttribute("planLoading", true);
                model.addAttribute("info", "Your plan is being generated...");
            }
        } else {
            // Plan doesn't exist yet
            Plan minimalPlan = new Plan();
            minimalPlan.setId(UUID.fromString(planId));
            minimalPlan.setDistanceKm(distanceKm);
            minimalPlan.setPlanLevel(planLevel);
            minimalPlan.setDaysPerWeek(daysPerWeek);
            model.addAttribute("plan", minimalPlan);
            model.addAttribute("planLoading", true);
            model.addAttribute("info", "Plan is being generated...");
        }

        return "training-plan-result";
    }

    // Optional: Add method to fetch plan content
//    @GetMapping("/plan-content")
//    @ResponseBody
//    public Optional<String> getPlanContent(@RequestParam String planId) {
//        try {
//            UUID planUUID = UUID.fromString(planId);
//            Optional<Plan> plan =  trainingPlanService.getPlan(planUUID);
//            return plan.get().getPlanJson().describeConstable();
//
//        } catch (Exception e) {
//            return ("Error loading plan: " + e.getMessage()).describeConstable();
//        }
//    }
}