package com.sqc.sos.dto.chat;

import lombok.Data;

import java.util.UUID;

@Data
public class StaffChatRequest {
    UUID tableId;
    String sessionId;
    String customerName;
    String sender;
    String message;
}
