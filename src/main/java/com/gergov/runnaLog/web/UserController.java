package com.gergov.runnaLog.web;

import com.gergov.runnaLog.run.service.RunService;
import com.gergov.runnaLog.security.UserData;
import com.gergov.runnaLog.stats.model.Stats;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.service.UserService;
import com.gergov.runnaLog.web.dto.CreateRunRequest;
import com.gergov.runnaLog.web.dto.EditProfileRequest;
import com.gergov.runnaLog.web.dto.RegisterRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RunService runService;

    @Autowired
    public UserController(UserService userService, RunService runService) {
        this.userService = userService;
        this.runService = runService;
    }

    // GET /users/{id}/profile - View profile page
    @GetMapping("/profile")
    public ModelAndView getProfilePage(@AuthenticationPrincipal UserData userData) {
        
        User user = userService.getById(userData.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile");

        // Add stats if needed for the profile page
        Stats stats = user.getStats();
        modelAndView.addObject("stats", stats);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    // GET /users/{id}/profile/edit - Show edit profile form
    @GetMapping("/profile/edit")
    public ModelAndView getProfileEditPage(@AuthenticationPrincipal UserData userData) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("edit-profile"); // This should match your edit profile template name

        User user = userService.getById(userData.getId());

        // Pre-populate the form with current user data
        EditProfileRequest editProfileRequest = EditProfileRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profilePicture(user.getProfilePicture())
                .build();

        modelAndView.addObject("user", user);
        modelAndView.addObject("editProfileRequest", editProfileRequest);

        return modelAndView;
    }

    // POST/PUT /users/{id}/profile - Handle profile update form submission
    @PostMapping("/profile") // You can use @PutMapping if you prefer, but POST is more common for forms
    public ModelAndView updateProfile(@Valid @ModelAttribute EditProfileRequest editProfileRequest,
                                      BindingResult bindingResult,
                                      @AuthenticationPrincipal UserData userData,
                                      RedirectAttributes redirectAttributes) {
        
        User user = userService.getById(userData.getId());

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("edit-profile");
            modelAndView.addObject("user", user);
            modelAndView.addObject("editProfileRequest", editProfileRequest);
            return modelAndView;
        }

        userService.updateUserProfile(userData.getId(), editProfileRequest);

        // Add success message
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");

        return new ModelAndView("redirect:/users/profile");
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getUsers() {
        List<User> users = userService.getAll();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("users");
        modelAndView.addObject("users", users);

        return modelAndView;
    }
}