package com.gergov.runnaLog.user.property;

import com.gergov.runnaLog.user.model.UserCountry;
import com.gergov.runnaLog.user.model.UserRole;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "users")
public class UserProperties {

    private DefaultUser defaultUser;

    private DefaultUser2 defaultUser2;

    private String testProperty;

    private String testProperty2;

    @Data
    public static class DefaultUser {

        private String username;

        private String email;

        private String password;

        private UserCountry country;

        private UserRole role;
    }

    @Data
    public static class DefaultUser2 {

        private String username;

        private String email;

        private String password;

        private UserCountry country;

        private UserRole role;
    }

}
