package com.sqc.sos.dto.menuitem;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MenuItemRequest {
    String name;
    String description;
    BigDecimal price;
    String imageUrl;
    Long categoryId;
    Boolean isAvailable;
    Boolean isActive;
} 