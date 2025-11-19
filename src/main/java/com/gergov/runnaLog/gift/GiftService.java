package com.gergov.runnaLog.gift;

import com.gergov.runnaLog.event.SuccessfulChargeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class GiftService {

    @Async
    @EventListener
    @Order(2)
    public  void sendGift(SuccessfulChargeEvent event) {

        System.out.printf("Sending 100 STR for subscription charge compensation for user with email [%s].", event.getEmail());
    }
}
