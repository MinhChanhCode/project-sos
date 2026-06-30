package com.sqc.sos.dto.cart;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;
import com.sqc.sos.model.CartItem;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {
    Long id;
    Long menuItemId;
    String menuItemName;
    String menuItemImageUrl;
    Integer quantity;
    BigDecimal unitPrice;
    BigDecimal subtotal;
    String notes;
    Boolean isActive;

    public static CartItemResponse fromEntity(CartItem entity) {
        return CartItemResponse.builder()
                .id(entity.getId())
                .menuItemId(entity.getMenuItem().getId())
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .notes(entity.getNotes())
                .isActive(entity.getIsActive())
                .build();
    }
} 