package com.sqc.sos.dto.invoice;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceResponse {
    Long id;
    Long orderId;
    UUID tableId;
    BigDecimal subtotal;
    BigDecimal tax;
    BigDecimal discount;
    BigDecimal total;
    String status;
    LocalDateTime createdAt;
    LocalDateTime paidAt;
}
