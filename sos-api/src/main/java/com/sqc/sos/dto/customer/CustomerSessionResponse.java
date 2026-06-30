package com.sqc.sos.dto.customer;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CustomerSessionResponse {
    String sessionId;
    UUID tableId;
    String tableName;
    String customerName;
    LocalDateTime updatedAt;
}
