package com.gergov.runnaLog.web;

import com.gergov.runnaLog.run.service.RunService;
import com.gergov.runnaLog.security.UserData;
import com.gergov.runnaLog.stats.model.Stats;
import com.gergov.runnaLog.subscription.model.Subscription;
import com.gergov.runnaLog.subscription.service.SubscriptionService;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.service.UserService;
import com.gergov.runnaLog.web.dto.DailyKmDto;
import com.gergov.runnaLog.web.dto.LoginRequest;
import com.gergov.runnaLog.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class IndexController {

    private final UserService userService;
    private final RunService runService;
    private final SubscriptionService subscriptionService;


    @Autowired
    public IndexController(UserService userService, RunService runService, SubscriptionService subscriptionService) {
        this.userService = userService;
        this.runService = runService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/")
    public String getIndexPage() {

        return "index";
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("loginRequest", new LoginRequest());

        return modelAndView;
    }

    // Form Handling steps:
    // 1. Return HTML form with empty object
    // 2. Use this empty object in the html form to fill the data
    // 3. Get the object filled with data via POST request
    // 4. Validate the received object - @Valid
    // 5. Capture all the validation errors if any exist - BindingResult
    // 6. Check if there are validation errors - if (bindingResult.hasErrors())
    //  - If there are errors -> show the same page and visualize errors - th:if="${#fields.hasErrors('username')}" th:errors="*{username}"
    //  - If there aren't any errors, display the next page ->

    @GetMapping("/register")
    public ModelAndView getRegisterPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        modelAndView.addObject("registerRequest", new RegisterRequest());

        return modelAndView;
    }

    // After POST, PUT, PATCH, DELETE requests we do "redirect:/endpoint"
    // Redirect = tells the client where to send the GET request
    @PostMapping("/register")
    public ModelAndView register(@Valid RegisterRequest registerRequest,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("register");
        }

        userService.register(registerRequest);
        redirectAttributes.addFlashAttribute("successfulRegistration",
                "You have registered successfully");

        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/feed")
    public ModelAndView getFeedPage(@AuthenticationPrincipal UserData userData) {

        User user = userService.getById(userData.getId());
        Stats stats = user.getStats();
        List<DailyKmDto> leaderboard = userService.getLeaderboard();

        boolean hasEliteSubscription = subscriptionService.hasActiveEliteSubscription(user);

        Subscription activeSubscription = subscriptionService.getActiveSubscription(user);

        return getFeedModelAndView(user, leaderboard, stats, hasEliteSubscription, activeSubscription);
    }

    private ModelAndView getFeedModelAndView(User user, List<DailyKmDto> leaderboard, Stats stats, boolean hasEliteSubscription, Subscription activeSubscription) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("feed");
        modelAndView.addObject("user", user);
        modelAndView.addObject("leaderboard", leaderboard);
        modelAndView.addObject("stats", stats);
        modelAndView.addObject("feedRuns", runService.getFeed(user));
        modelAndView.addObject("hasEliteSubscription", hasEliteSubscription);
        modelAndView.addObject("activeSubscription", activeSubscription);

        return modelAndView;
    }
}
