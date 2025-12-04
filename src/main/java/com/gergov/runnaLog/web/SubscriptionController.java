package com.gergov.runnaLog.web;

import com.gergov.runnaLog.security.UserData;
import com.gergov.runnaLog.stats.model.Stats;
import com.gergov.runnaLog.subscription.model.Subscription;
import com.gergov.runnaLog.subscription.service.SubscriptionService;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.service.UserService;
import com.gergov.runnaLog.web.dto.SubscriptionUpgradeRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

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

        Subscription activeSubscription = subscriptionService.getActiveSubscription(user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("subscriptions");
        modelAndView.addObject("subscriptionUpgradeRequest", new SubscriptionUpgradeRequest());
        modelAndView.addObject("userSub", activeSubscription);
        modelAndView.addObject("stats", stats);

        return modelAndView;
    }

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
            @Valid SubscriptionUpgradeRequest subscriptionUpgradeRequest,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserData userData) {

        User user = userService.getById(userData.getId());
        Stats stats = user.getStats();

        if (bindingResult.hasErrors()) {
            return getSubscriptionPurchaseModelAndView("Invalid subscription data. Please try again.", stats);
        }

        boolean success = subscriptionService.purchaseSubscription(user, subscriptionUpgradeRequest.getType());

        if (success) {
            return new ModelAndView("redirect:/subscriptions?success");
        } else {
            return getSubscriptionPurchaseModelAndView("Not enough STR to purchase this plan.", stats);
        }
    }

    private static ModelAndView getSubscriptionPurchaseModelAndView(String attributeValue, Stats stats) {
        ModelAndView mav = new ModelAndView("subscriptions");
        mav.addObject("error", attributeValue);
        mav.addObject("stats", stats);
        mav.addObject("subscriptionUpgradeRequest", new SubscriptionUpgradeRequest());
        return new ModelAndView("redirect:/subscriptions?error=" + attributeValue);
    }
}
