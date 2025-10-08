package com.gergov.runnaLog.web;

import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.property.UserProperties;
import com.gergov.runnaLog.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/subscriptions")
public class SubscriptionController {


    private final UserService userService;
    private final UserProperties userProperties;

    @Autowired
    public SubscriptionController(UserService userService, UserProperties userProperties) {
        this.userService = userService;
        this.userProperties = userProperties;
    }

    @GetMapping
    public String getUpgradePage() {

        return "subscriptions";
    }

    // /subscriptions/history
    @GetMapping("/history")
    public ModelAndView getSubscriptionHistoryPage() {

        User user = userService.getByUsername(userProperties.getDefaultUser().getUsername());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("subscription-history");
        modelAndView.addObject("user", user);

        return modelAndView;
    }
}
