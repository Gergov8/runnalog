package com.gergov.runnaLog.web;

import com.gergov.runnaLog.stats.model.Stats;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // /users {id}/profile
    @GetMapping("/{id}/profile")
    public ModelAndView getProfilePage(@PathVariable UUID id) {

        User user = userService.getById(id);
        Stats stats = user.getStats();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile");
        modelAndView.addObject("user", user);
        modelAndView.addObject("stats", stats);

        return modelAndView;
    }

    // /users {id}/profile
    @GetMapping("/{id}/profile/edit")
    public ModelAndView getProfileEditPage(@PathVariable UUID id) {

        User user = userService.getById(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("edit-profile");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PostMapping("/{id}/profile")
    public String getUpdatedProfile(@PathVariable UUID id,
                                @ModelAttribute User formUser,
                                RedirectAttributes redirectAttributes) {

        userService.updateUserProfile(id, formUser);
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");

        return "redirect:/users/" + id + "/profile";
    }

    @GetMapping
    public ModelAndView getUsers() {

        List<User> users = userService.getAll();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("users");
        modelAndView.addObject("users", users);

        return modelAndView;
    }
}
