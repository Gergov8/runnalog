package com.gergov.runnaLog.web;

import com.gergov.runnaLog.security.UserData;
import com.gergov.runnaLog.stats.model.Stats;
import com.gergov.runnaLog.subscription.model.SubscriptionPeriod;
import com.gergov.runnaLog.subscription.model.SubscriptionType;
import com.gergov.runnaLog.subscription.service.SubscriptionService;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.service.UserService;
import com.gergov.runnaLog.web.dto.SubscriptionUpgradeRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/subscriptions")
public class SubscriptionController {


    private final UserService userService;
    private final SubscriptionService subscriptionService;


    @Autowired
    public SubscriptionController(UserService userService, SubscriptionService subscriptionService) {
        this.userService = userService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    public ModelAndView getUpgradePage(@AuthenticationPrincipal UserData userData) {


        User user = userService.getById(userData.getId());
        Stats stats = user.getStats();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("subscriptions");
        modelAndView.addObject("subscriptionUpgradeRequest", new SubscriptionUpgradeRequest());
        modelAndView.addObject("stats", stats);

        return modelAndView;
    }

    // /subscriptions/history
    @GetMapping("/history")
    public ModelAndView getSubscriptionHistoryPage(@AuthenticationPrincipal UserData userData) {

        User user = userService.getById(userData.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("subscription-history");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PostMapping("/purchase")
    public ModelAndView purchaseSubscription(
            @RequestParam("type") SubscriptionType type,
            @RequestParam(value = "period", defaultValue = "MONTHLY") SubscriptionPeriod period,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        UUID userId = (UUID) session.getAttribute("userId");
        User user = userService.getById(userId);

        boolean success = subscriptionService.purchaseSubscription(user, type, period);

        ModelAndView modelAndView = new ModelAndView("subscriptions");

        if (success) {
            modelAndView.addObject("message", "Subscription purchased successfully!");
        } else {
            modelAndView.addObject("error", "Not enough STR to purchase this plan.");
        }

        modelAndView.addObject("subscriptionUpgradeRequest", new SubscriptionUpgradeRequest());
        return modelAndView;
    }
}
