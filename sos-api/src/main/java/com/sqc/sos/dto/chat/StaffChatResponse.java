package com.sqc.sos.dto.chat;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class StaffChatResponse {
    Long id;
    UUID tableId;
    String tableName;
    String sessionId;
    String customerName;
    String sender;
    String message;
    LocalDateTime createdAt;
}
