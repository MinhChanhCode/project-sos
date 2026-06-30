package com.sqc.sos.dto.orderitem;

import lombok.Data;
import com.sqc.sos.model.OrderItemStatus;

@Data
public class UpdateOrderItemStatusRequest {
    private OrderItemStatus status;
    private Integer completedQuantity; // Số lượng đã hoàn thành (optional)
} 