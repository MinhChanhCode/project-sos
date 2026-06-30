package com.sqc.sos.dto.table;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableDetailResponse {
    UUID tableId;
    String tableName;
    Integer customers;
    String status;
    BigDecimal totalAmount;
    Long activeOrderId; // Order đang hoạt động (nếu có)
    List<TableOrderItemSummary> sessionItems; // Items từ giỏ hàng đang hoạt động (session hiện tại)
}


