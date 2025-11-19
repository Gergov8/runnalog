package com.gergov.runnaLog.email;

import com.gergov.runnaLog.event.SuccessfulChargeEvent;
import com.gergov.runnaLog.event.SuccessfulRegistrationEvent;
import com.gergov.runnaLog.user.model.User;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Async
    @EventListener
    public void  sendEmailForRegistration(SuccessfulRegistrationEvent event) {
        System.out.printf("Sending email for successful registration of new user with email [%s]", event.getEmail());
    }

    @Async
    @EventListener
    @Order(1)
    public void  sendEmailForSubscription(SuccessfulChargeEvent event) {
        System.out.printf("Sending email for successful payment for new subscription of user with email [%s]", event.getEmail());
    }

    public void sendReminderEmail(User user) {

        System.out.printf("Email sent to [%s] with username [%s].\n", user.getRole(), user.getUsername());
    }
}
