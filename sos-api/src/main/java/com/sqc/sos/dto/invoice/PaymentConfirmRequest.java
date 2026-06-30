package com.sqc.sos.dto.invoice;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentConfirmRequest {
    Long orderId;
    String method;
    BigDecimal amount;
}
