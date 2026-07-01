package com.sqc.sos.dto.invoice;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
    String restaurantName;
    String tableName;
    String customerName;
    String invoiceCode;
    BigDecimal subtotal;
    BigDecimal tax;
    BigDecimal discount;
    BigDecimal serviceFee;
    BigDecimal total;
    String status;
    String paymentQrPayload;
    List<InvoiceItemResponse> items;
    LocalDateTime createdAt;
    LocalDateTime paidAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InvoiceItemResponse {
        Long orderItemId;
        Long menuItemId;
        String menuItemName;
        Integer quantity;
        BigDecimal unitPrice;
        BigDecimal lineTotal;
        String notes;
    }
}
