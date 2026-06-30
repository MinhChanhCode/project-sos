package com.sqc.sos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class CartRealtimeListener {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleCartUpdated(CartUpdatedEvent event) {
        messagingTemplate.convertAndSend(
            "/topic/tables/" + event.getTableId() + "/cart",
            Map.of("type", "CART_UPDATED")
        );
    }
}
