package com.sqc.sos.dto.menuitem;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MenuItemResponse {
    Long id;
    String name;
    String description;
    BigDecimal price;
    String imageUrl;
    Long categoryId;
    String categoryName;
    Boolean isAvailable;
    Boolean isActive;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    // Promotion fields
    BigDecimal originalPrice;
    BigDecimal promotionalPrice;
    LocalDateTime promotionEndDate;
} 