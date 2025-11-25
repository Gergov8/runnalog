package com.gergov.runnaLog.web;

import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.security.UserData;
import com.gergov.runnaLog.stats.model.Stats;
import com.gergov.runnaLog.subscription.model.Subscription;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.service.UserService;
import com.gergov.runnaLog.web.dto.EditProfileRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
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

    // GET /users/{id}/profile - View profile page
    @GetMapping("/profile")
    public ModelAndView getProfilePage(@AuthenticationPrincipal UserData userData) {
        
        User user = userService.getById(userData.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile");

        Stats stats = user.getStats();
        List<Run> runs = user.getRuns();
        List<Subscription> subs = user.getSubscriptions();
        LocalDateTime date = LocalDateTime.now();
        modelAndView.addObject("stats", stats);
        modelAndView.addObject("runs", runs);
        modelAndView.addObject("subs", subs);
        modelAndView.addObject("date", date);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @DeleteMapping("/delete/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView deleteRun(@AuthenticationPrincipal UserData userData,
                                  @PathVariable UUID userId) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject(userData.getId());
        userService.deleteUser(userId, userData.getId());

        return new ModelAndView("redirect:/users");
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

    @PutMapping("/role/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String switchUserRole(@PathVariable UUID userId) {

        userService.switchRole(userId);

        return "redirect:/users";
    }
}