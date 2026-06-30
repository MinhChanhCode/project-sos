package com.sqc.sos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class OrderItemStatusRealtimeListener {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleOrderItemStatusChanged(OrderItemStatusChangedEvent event) {
        // Deprecated: realtime for management is now sent from RealtimeService with full payload
    }
}
