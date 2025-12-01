package com.gergov.runnaLog.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gergov.runnaLog.exception.UserNeedsEliteSubscriptionException;
import com.gergov.runnaLog.feign.dto.Plan;
import com.gergov.runnaLog.subscription.service.SubscriptionService;
import com.gergov.runnaLog.trainingPlan.service.TrainingPlanService;
import com.gergov.runnaLog.security.UserData;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.model.UserRole;
import com.gergov.runnaLog.user.service.UserService;
import com.gergov.runnaLog.feign.dto.CreatePlanRequest;
import com.gergov.runnaLog.feign.dto.CreatePlanResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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
    public String trainingPlansDashboard(
            Model model,
            @AuthenticationPrincipal UserData userData,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "connectionStatus", required = false) String connectionStatus,
            @RequestParam(value = "planId", required = false) String planId) {

        User user = userService.getById(userData.getId());

        if (!subscriptionService.hasActiveEliteSubscription(user) && user.getRole() != UserRole.ADMIN) {
            throw new UserNeedsEliteSubscriptionException("You need an Elite subscription to access this feature.");
        }
        enrichPlanDashboardModel(model, message, error, connectionStatus, planId, user);

        return "training-plans";
    }

    @PostMapping("/create")
    public String createTrainingPlan(
            @ModelAttribute CreatePlanRequest createPlanRequest,
            @AuthenticationPrincipal UserData userData) {

        createPlanRequest.setUserId(userData.getId());
        CreatePlanResponse response = trainingPlanService.createTrainingPlan(createPlanRequest);

        return "redirect:/training/plan-result?planId=" + response.getId();
    }

    @PostMapping("/delete/{id}")
    public String deleteTrainingPlan(
            @PathVariable UUID id,
            RedirectAttributes redirectAttributes) {

        trainingPlanService.deletePlan(id);
        redirectAttributes.addFlashAttribute("message", "Training plan deleted successfully!");

        return "redirect:/training/plans";
    }

    @GetMapping("/plan-result")
    public String showTrainingPlanResult(
            Model model,
            @AuthenticationPrincipal UserData userData,
            @RequestParam String planId) throws JsonProcessingException {

        User user = userService.getById(userData.getId());
        enrichPlanResultModel(model, planId, user);

        return "training-plan-result";
    }

    private void enrichPlanResultModel(Model model, String planId, User user) throws JsonProcessingException {
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Your Training Plan");
        model.addAttribute("planId", planId);

        Optional<Plan> planOptional = trainingPlanService.getPlan(UUID.fromString(planId));

        if (planOptional.isPresent()) {
            Plan plan = planOptional.get();
            model.addAttribute("plan", plan);

            if (plan.getPlanJson() != null && !plan.getPlanJson().trim().isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode planJson = objectMapper.readTree(plan.getPlanJson());
                model.addAttribute("planJson", planJson);
                model.addAttribute("planReady", true);
            }
        }
    }

    private void enrichPlanDashboardModel(Model model, String message, String error, String connectionStatus, String planId, User user) {
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Training Plans Dashboard");
        model.addAttribute("createPlanRequest", new CreatePlanRequest());
        model.addAttribute("userPlans", trainingPlanService.getUserPlans(user.getId()));
        model.addAttribute("plansCount", trainingPlanService.getUserPlanCount(user.getId()));

        if (message != null) {
           throw new AccessDeniedException("You need an Elite subscription to access this feature");
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        if (connectionStatus != null) {
            model.addAttribute("connectionStatus", connectionStatus);
        }

        model.addAttribute("planId", planId != null ? planId : "");
    }
}