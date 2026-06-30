package com.sqc.sos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class TableStatusRealtimeListener {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleTableStatusChanged(TableStatusChangedEvent event) {
        messagingTemplate.convertAndSend(
            "/topic/management/tables",
            Map.of("type", "TABLE_STATUS_CHANGED")
        );
    }
}
