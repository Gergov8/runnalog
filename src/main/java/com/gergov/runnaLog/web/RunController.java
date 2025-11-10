package com.gergov.runnaLog.web;

import com.gergov.runnaLog.like.service.LikeService;
import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.service.RunService;
import com.gergov.runnaLog.security.UserData;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.service.UserService;
import com.gergov.runnaLog.web.dto.CreateRunRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/runs")
public class RunController {


    private final RunService runService;
    private final UserService userService;
    private final LikeService likeService;


    @Autowired
    public RunController(RunService runService, UserService userService, LikeService likeService) {
        this.runService = runService;
        this.userService = userService;
        this.likeService = likeService;
    }

    @GetMapping("/{userId}/add")
    public ModelAndView getAddRunPage(@PathVariable UUID userId) {

        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("add-run");
        modelAndView.addObject("createRunRequest", new CreateRunRequest());
        modelAndView.addObject("userId", userId);

        return modelAndView;
    }

    @PostMapping("/{userId}/add")
    public ModelAndView createRun(@PathVariable UUID userId,
                                  @Valid CreateRunRequest createRunRequest,
                                  BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("add-run");
            modelAndView.addObject("userId", userId);
            return modelAndView;
        }

        User user = userService.getById(userId);

        runService.createRun(createRunRequest, user);

        return new ModelAndView("redirect:/feed");
    }

    @GetMapping("/{userId}")
    public ModelAndView getUserRuns(@PathVariable UUID userId) {

        User user = userService.getById(userId);
        List<Run> runs = runService.getRunsByUser(user);

        ModelAndView modelAndView = new ModelAndView("user-runs");
        modelAndView.addObject("user", user);
        modelAndView.addObject("runs", runs);

        return modelAndView;
    }


}
