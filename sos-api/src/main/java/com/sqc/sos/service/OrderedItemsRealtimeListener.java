package com.sqc.sos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class OrderedItemsRealtimeListener {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleOrderedItemsUpdated(OrderedItemsUpdatedEvent event) {
        messagingTemplate.convertAndSend(
            "/topic/tables/" + event.getTableId() + "/ordered",
            Map.of("type", "ORDERED_UPDATED")
        );
    }
}
