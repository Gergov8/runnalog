package com.gergov.runnaLog.web;

import com.gergov.runnaLog.comment.model.Comment;
import com.gergov.runnaLog.comment.service.CommentService;
import com.gergov.runnaLog.like.service.LikeService;
import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.service.RunService;
import com.gergov.runnaLog.security.UserData;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.service.UserService;
import com.gergov.runnaLog.web.dto.AddCommentRequest;
import com.gergov.runnaLog.web.dto.CreateRunRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/runs")
public class RunController {


    private final RunService runService;
    private final UserService userService;
    private final LikeService likeService;
    private final CommentService commentService;


    @Autowired
    public RunController(RunService runService, UserService userService, LikeService likeService, CommentService commentService) {
        this.runService = runService;
        this.userService = userService;
        this.likeService = likeService;
        this.commentService = commentService;
    }

    @GetMapping("/add")
    public ModelAndView getAddRunPage(@AuthenticationPrincipal UserData userData) {

        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("add-run");
        modelAndView.addObject("createRunRequest", new CreateRunRequest());
        modelAndView.addObject("userId", userData.getId());

        return modelAndView;
    }

    @PostMapping("/add")
    public ModelAndView createRun(@AuthenticationPrincipal UserData userData,
                                  @Valid CreateRunRequest createRunRequest,
                                  BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("add-run");
            modelAndView.addObject("userId", userData.getId());

            return modelAndView;
        }

        User user = userService.getById(userData.getId());

        runService.createRun(createRunRequest, user);

        return new ModelAndView("redirect:/feed");
    }

    @GetMapping("/{runId}/details")
    ModelAndView getRunDetails(@PathVariable UUID runId,
                               Principal principal,
                               AddCommentRequest addCommentRequest) {


        Run run = runService.getRunById(runId);
        User user = run.getUser();
        String username = principal.getName();
        UserData currentUser = (UserData) userService.loadUserByUsername(username);

        return getRunDetailsModelAndView(runId, addCommentRequest, run, user, currentUser);
    }

    @DeleteMapping("/{userId}/delete/{runId}")
    public ModelAndView deleteRun(@AuthenticationPrincipal UserData userData,
                                  @PathVariable UUID userId,
                                  @PathVariable UUID runId) {

        User currentUser = userService.getById(userData.getId());

        runService.deleteRun(currentUser, runId);

        return new ModelAndView("redirect:/runs/" + userId);
    }

    @GetMapping("/{userId}")
    public ModelAndView getUserRuns(@PathVariable("userId") UUID userId, @AuthenticationPrincipal UserData currentUser) {

        User user = userService.getById(userId);
        List<Run> runs = runService.getRunsByUser(user);

        User loggedUser = userService.getById(currentUser.getId()); // currently logged in

        ModelAndView modelAndView = new ModelAndView("user-runs");
        modelAndView.addObject("user", user);
        modelAndView.addObject("loggedUser", loggedUser);
        modelAndView.addObject("runs", runs);

        return modelAndView;
    }

    @PostMapping("/{runId}/like")
    public String likeRun(@PathVariable UUID runId,
                          @AuthenticationPrincipal UserData userData) {

        User currentUser = userService.getById(userData.getId());
        likeService.likeRun(runId, currentUser);
        return "redirect:/feed"; // Redirect back to feed
    }

    @PostMapping("/{runId}/unlike")
    public String unlikeRun(@PathVariable UUID runId,
                            @AuthenticationPrincipal UserData userData) {

        User currentUser = userService.getById(userData.getId());
        likeService.unlikeRun(runId, currentUser);
        return "redirect:/feed"; // Redirect back to feed
    }

    @PostMapping("/{runId}/comments")
    public String addComment(@PathVariable UUID runId,
                             @AuthenticationPrincipal UserData userData,
                             @Valid AddCommentRequest addCommentRequest,
                             BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "redirect:/runs/" + runId + "/details?error=Comment cannot be empty";
        }

        User currentUser = userService.getById(userData.getId());
        commentService.addComment(runId, addCommentRequest, currentUser);

        return "redirect:/runs/" + runId + "/details";
    }

    @DeleteMapping("/{runId}/details/delete/{commentId}")
    public ModelAndView deleteComment(@AuthenticationPrincipal UserData userData,
                                      @PathVariable UUID runId,
                                      @PathVariable UUID commentId) {

        Run run = runService.getRunById(runId);
        Comment comment = commentService.getCommentById(commentId);
        User currentUser = userService.getById(userData.getId());

        commentService.deleteComment(currentUser, run, comment);

        return new ModelAndView("redirect:/runs/" + runId + "/details");
    }

    private ModelAndView getRunDetailsModelAndView(UUID runId, AddCommentRequest addCommentRequest, Run run, User user, UserData currentUser) {
        ModelAndView modelAndView = new ModelAndView("run-details");
        modelAndView.addObject("run", run);
        modelAndView.addObject("user", user);
        modelAndView.addObject("currentUser", currentUser);
        modelAndView.addObject("addCommentRequest", addCommentRequest);
        modelAndView.addObject("comments", commentService.getCommentsForRun(runId));


        return modelAndView;
    }

}
