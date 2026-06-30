package com.sqc.sos.dto.customer;

import lombok.Data;

import java.util.UUID;

@Data
public class CustomerSessionRequest {
    String sessionId;
    UUID tableId;
    String customerName;
}
