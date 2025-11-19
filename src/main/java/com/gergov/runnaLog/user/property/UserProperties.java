package com.gergov.runnaLog.user.property;

import com.gergov.runnaLog.user.model.UserCountry;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "users")
public class UserProperties {

    private DefaultUser defaultUser;

    private String testProperty;

    @Data
    public static class DefaultUser {

        private String username;

        private String email;

        private String password;

        private UserCountry country;
    }

}
