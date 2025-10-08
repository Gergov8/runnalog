package com.gergov.runnaLog.web;

import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.service.RunService;
import com.gergov.runnaLog.stats.model.Stats;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.property.UserProperties;
import com.gergov.runnaLog.user.service.UserService;
import com.gergov.runnaLog.web.dto.CreateRunRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {

    private final UserService userService;
    private final UserProperties userProperties;

    @Autowired
    public IndexController(UserService userService, UserProperties userProperties) {
        this.userService = userService;
        this.userProperties = userProperties;
    }

    @GetMapping("/")
    public String getIndexPage() {

        return "index";
    }

    @GetMapping("/login")
    public String getLoginPage() {

        return "login";
    }

    @GetMapping("/register")
    public String getRegisterPage() {

        return "register";
    }

    @GetMapping("/add")
    public String getAddRunPage() {

        return "add-run";
    }

    @GetMapping("/feed")
    public ModelAndView getFeedPage() {

        User user = userService.getByUsername(userProperties.getDefaultUser().getUsername());

        ModelAndView modelAndView = new ModelAndView();
        Stats stats = user.getStats();

        modelAndView.setViewName("feed");
        modelAndView.addObject("user", user);
        modelAndView.addObject("stats", stats);

        return modelAndView;
    }

//    @GetMapping("/profile")
//    public ModelAndView getProfilePage() {
//
//        User user = userService.getByUsername(userProperties.getDefaultUser().getUsername());
//        Stats stats = user.getStats();
//
//        ModelAndView modelAndView = new ModelAndView();
//
//        modelAndView.setViewName("profile");
//        modelAndView.addObject("user", user);
//        modelAndView.addObject("stats", stats);
//
//        return modelAndView;
//    }

}
