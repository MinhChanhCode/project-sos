package com.sqc.sos.dto.chat;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatRequest {
    String sessionId;
    String tableId;
    String tableNumber;
    String customerName;
    Long orderId;
    String message;
}
