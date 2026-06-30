package com.sqc.sos.dto.orderitem;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderItemResponse {
    private Long id;
    private Long menuItemId;
    private String menuItemName;
    private Integer pendingQuantity;
    private Integer preparingQuantity;
    private Integer completedQuantity; // Số lượng đã hoàn thành
    private Integer servedQuantity;
    private Integer cancelledQuantity;
    private Integer outOfStockQuantity;
    private Integer totalQuantity; // Tổng số lượng có tính tiền (trừ hủy & hết món)
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Long orderId;
    private String tableName;
    private String notes; // Ghi chú cho món ăn
    private LocalDateTime orderTime; // Thời gian tạo order
} 