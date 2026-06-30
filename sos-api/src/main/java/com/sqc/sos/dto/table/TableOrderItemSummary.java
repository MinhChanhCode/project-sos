package com.sqc.sos.dto.table;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableOrderItemSummary {
    Long id;
    Long menuItemId;
    String menuItemName;
    String menuItemImageUrl;
    Integer pendingQuantity;
    Integer preparingQuantity;
    Integer completedQuantity;
    Integer servedQuantity;
    Integer cancelledQuantity;
    Integer outOfStockQuantity;
    Integer totalQuantity;
    BigDecimal unitPrice;
    String status;
    Long orderId; // ID của order chứa item này
    String orderStatus; // Trạng thái của order
    LocalDateTime orderTime; // Thời gian tạo order
    String notes; // Ghi chú cho món ăn
}


