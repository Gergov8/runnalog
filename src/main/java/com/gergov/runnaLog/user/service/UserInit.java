package com.gergov.runnaLog.user.service;

import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.property.UserProperties;
import com.gergov.runnaLog.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class UserInit implements ApplicationRunner {

    private final UserService userService;
    private final UserProperties userProperties;

    @Autowired
    public UserInit(UserService userService, UserProperties userProperties) {
        this.userService = userService;
        this.userProperties = userProperties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        List<User> users = userService.getAll();

        boolean defaultUserDoesNotExist = users.stream().noneMatch(user -> user.getUsername().equals(userProperties.getDefaultUser().getUsername()));

        if (defaultUserDoesNotExist) {

            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username(userProperties.getDefaultUser().getUsername())
                    .email(userProperties.getDefaultUser().getEmail())
                    .password(userProperties.getDefaultUser().getPassword())
                    .country(userProperties.getDefaultUser().getCountry())
                    .build();

            userService.register(registerRequest);
        }
    }
}
