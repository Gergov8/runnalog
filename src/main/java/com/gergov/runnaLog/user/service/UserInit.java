package com.gergov.runnaLog.user.service;

import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.model.RunVisibility;
import com.gergov.runnaLog.run.service.RunService;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.model.UserRole;
import com.gergov.runnaLog.user.property.UserProperties;
import com.gergov.runnaLog.web.dto.CreateRunRequest;
import com.gergov.runnaLog.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Profile("!test")
public class UserInit implements ApplicationRunner {

    private final UserService userService;
    private final UserProperties userProperties;
    private final RunService runService;


    @Autowired
    public UserInit(UserService userService, UserProperties userProperties, RunService runService) {
        this.userService = userService;
        this.userProperties = userProperties;
        this.runService = runService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        List<User> adminUsers = userService.getAllAdmin();

        boolean defaultAdminUserDoesNotExist = adminUsers.stream().noneMatch(user -> user.getRole().equals(UserRole.ADMIN));

        if (defaultAdminUserDoesNotExist) {

            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username(userProperties.getDefaultUser().getUsername())
                    .email(userProperties.getDefaultUser().getEmail())
                    .password(userProperties.getDefaultUser().getPassword())
                    .country(userProperties.getDefaultUser().getCountry())
                    .build();

            userService.createAdmin(registerRequest);
        }

        List<User> users = userService.getAll();

        boolean defaultNormalUserDoesNotExist = users.stream().noneMatch(user -> user.getRole().equals(UserRole.USER));

        if (defaultNormalUserDoesNotExist) {

            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username(userProperties.getDefaultUser2().getUsername())
                    .email(userProperties.getDefaultUser2().getEmail())
                    .password(userProperties.getDefaultUser2().getPassword())
                    .country(userProperties.getDefaultUser2().getCountry())
                    .build();

            userService.createDefaultUser(registerRequest);
        }

        List<Run> runsOfAdmin = runService.getRunsByUser(userService.getByUsername(userProperties.getDefaultUser().getUsername()));

        boolean noRunsExistForAdmin = runsOfAdmin.isEmpty();

        if (noRunsExistForAdmin) {

            CreateRunRequest createRunRequest = CreateRunRequest.builder()
                    .distance(10.0)
                    .durationHours(0)
                    .durationMinutes(50)
                    .durationSeconds(0)
                    .title("Morning Run")
                    .description("An easy jog to start the day")
                    .visibility(RunVisibility.PUBLIC)
                    .build();

            User user = userService.getByUsername(userProperties.getDefaultUser().getUsername());

            runService.createRun(createRunRequest, user);
        }

        List<Run> runsOfUser = runService.getRunsByUser(userService.getByUsername(userProperties.getDefaultUser2().getUsername()));

        boolean noRunsExistForUser = runsOfUser.isEmpty();

        if (noRunsExistForUser) {

            CreateRunRequest createRunRequest = CreateRunRequest.builder()
                    .distance(5.0)
                    .durationHours(0)
                    .durationMinutes(31)
                    .durationSeconds(34)
                    .title("Evening Run")
                    .description("An easy jog to end the day")
                    .visibility(RunVisibility.PUBLIC)
                    .build();

            User user = userService.getByUsername(userProperties.getDefaultUser2().getUsername());

            runService.createRun(createRunRequest, user);
        }
    }
}
