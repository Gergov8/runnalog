package com.gergov.runnaLog.web;

import com.gergov.runnaLog.comment.service.CommentService;
import com.gergov.runnaLog.like.service.LikeService;
import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.service.RunService;
import com.gergov.runnaLog.security.UserData;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.model.UserRole;
import com.gergov.runnaLog.user.service.UserService;
import com.gergov.runnaLog.web.dto.AddCommentRequest;
import com.gergov.runnaLog.web.dto.CreateRunRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RunController.class)
class RunControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RunService runService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private LikeService likeService;

    @MockitoBean
    private CommentService commentService;

    @Test
    void getAddRunPage_ShouldReturnAddRunView() throws Exception {
        UUID userId = UUID.randomUUID();
        UserData userData = new UserData(userId, "testuser", "password", UserRole.USER, true);

        mockMvc.perform(get("/runs/add").with(user(userData)))
                .andExpect(status().isOk())
                .andExpect(view().name("add-run"))
                .andExpect(model().attributeExists("createRunRequest"))
                .andExpect(model().attributeExists("userId"));
    }

    @Test
    void createRun_ShouldRedirectToFeed_WhenValid() throws Exception {
        UUID userId = UUID.randomUUID();
        UserData userData = new UserData(userId, "testuser", "password", UserRole.USER, true);

        User user = new User();
        user.setId(userId);

        when(userService.getById(userId)).thenReturn(user);

        mockMvc.perform(post("/runs/add")
                        .param("distance", "5.0")
                        .param("duration", "45")
                        .param("title", "Morning run")
                        .param("visibility", "PUBLIC")
                        .with(user(userData))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feed"));

        verify(runService, times(1)).createRun(any(CreateRunRequest.class), eq(user));
    }

    @Test
    void getRunDetails_ShouldReturnRunDetailsView() throws Exception {
        UUID runId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserData userData = new UserData(userId, "testuser", "password", UserRole.USER, true);

        Run run = new Run();
        run.setId(runId);
        User runUser = new User();
        runUser.setId(userId);
        run.setUser(runUser);
        run.setDistance(5.0);
        Duration duration = Duration.ofMinutes(23).plusSeconds(30);
        run.setDuration(duration);

        when(runService.getRunById(runId)).thenReturn(run);
        when(userService.loadUserByUsername("testuser")).thenReturn(userData);
        when(commentService.getCommentsForRun(runId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/runs/" + runId + "/details").with(user(userData)))
                .andExpect(status().isOk())
                .andExpect(view().name("run-details"))
                .andExpect(model().attributeExists("run"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(model().attributeExists("comments"))
                .andExpect(model().attributeExists("addCommentRequest"));
    }

    @Test
    void likeRun_ShouldRedirectToFeed() throws Exception {
        UUID runId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserData userData = new UserData(userId, "testuser", "password", UserRole.USER, true);

        User user = new User();
        user.setId(userId);
        when(userService.getById(userId)).thenReturn(user);

        mockMvc.perform(post("/runs/" + runId + "/like").with(user(userData)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feed"));

        verify(likeService, times(1)).likeRun(runId, user);
    }

    @Test
    void unlikeRun_ShouldRedirectToFeed() throws Exception {
        UUID runId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserData userData = new UserData(userId, "testuser", "password", UserRole.USER, true);

        User user = new User();
        user.setId(userId);
        when(userService.getById(userId)).thenReturn(user);

        mockMvc.perform(post("/runs/" + runId + "/unlike").with(user(userData)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feed"));

        verify(likeService, times(1)).unlikeRun(runId, user);
    }

    @Test
    void addComment_ShouldRedirectToRunDetails_WhenValid() throws Exception {
        UUID runId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserData userData = new UserData(userId, "testuser", "password", UserRole.USER, true);

        User user = new User();
        user.setId(userId);
        when(userService.getById(userId)).thenReturn(user);

        mockMvc.perform(post("/runs/" + runId + "/comments")
                        .param("content", "Great run!")
                        .with(user(userData))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/runs/" + runId + "/details"));

        verify(commentService, times(1)).addComment(eq(runId), any(AddCommentRequest.class), eq(user));
    }

    @Test
    void deleteRun_ShouldRedirectToUserRuns() throws Exception {
        UUID runId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserData userData = new UserData(userId, "testuser", "password", UserRole.USER, true);

        User user = new User();
        user.setId(userId);
        when(userService.getById(userId)).thenReturn(user);

        mockMvc.perform(delete("/runs/" + userId + "/delete/" + runId).with(user(userData)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/runs/" + userId));

        verify(runService, times(1)).deleteRun(user, runId);
    }
}
