package com.gergov.runnaLog.web;

import com.gergov.runnaLog.feign.dto.CreatePlanRequest;
import com.gergov.runnaLog.feign.dto.CreatePlanResponse;
import com.gergov.runnaLog.feign.dto.Plan;
import com.gergov.runnaLog.security.UserData;
import com.gergov.runnaLog.trainingPlan.service.TrainingPlanService;
import com.gergov.runnaLog.user.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlanController.class)
class PlanControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainingPlanService planService;

    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void sayHello_ShouldReturnHelloString() throws Exception {
        mockMvc.perform(get("/api/v1/plans/say-hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello from PlanController"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void getPlan_ShouldReturnNotFound_WhenPlanDoesNotExist() throws Exception {
        UUID planId = UUID.randomUUID();

        when(planService.getPlan(planId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/plans/" + planId))
                .andExpect(status().isNotFound());

        verify(planService).getPlan(planId);
    }
}
