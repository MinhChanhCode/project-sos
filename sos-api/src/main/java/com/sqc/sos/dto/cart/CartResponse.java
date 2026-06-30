package com.sqc.sos.dto.cart;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartResponse {
    Long id;
    UUID tableId;
    String tableName;
    String sessionId;
    List<CartItemResponse> items;
    Integer totalItems;
    BigDecimal totalAmount;
    Boolean isActive;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
} 