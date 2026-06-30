package com.sqc.sos.dto.table;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableListItemResponse {
    UUID id;
    String name;
    Integer capacity;
    Boolean isAvailable;
    String status;
    java.math.BigDecimal totalAmount;
    Integer customers;
    Long activeOrderId;
    Long areaId;
    Integer posX;
    Integer posY;
    String tableStatus;
}


