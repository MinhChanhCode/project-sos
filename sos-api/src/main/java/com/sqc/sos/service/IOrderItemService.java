package com.sqc.sos.service;

import com.sqc.sos.dto.orderitem.OrderItemStatusResponse;
import com.sqc.sos.dto.orderitem.OrderItemResponse;
import com.sqc.sos.dto.orderitem.UpdateOrderItemStatusRequest;
import com.sqc.sos.model.OrderItemStatus;
import java.util.List;

public interface IOrderItemService {
    List<OrderItemStatusResponse> getAllStatuses();
    void updateStatus(Long orderItemId, OrderItemStatus status);
    void updateStatusPartial(Long orderItemId, OrderItemStatus status, Integer quantity);
    void updateMultipleStatuses(List<Long> orderItemIds, OrderItemStatus status);
    List<OrderItemResponse> getOrderItemsByOrderId(Long orderId);
    OrderItemResponse getOrderItemById(Long orderItemId);
    List<OrderItemResponse> getPendingOrderItemsForManagement();
    
    /**
     * Merge các order items duplicate trong cùng một order
     */
    void mergeDuplicateOrderItems(Long orderId);
    
    /**
     * Thêm món mới vào order với logic merge tự động
     */
    OrderItemResponse addItemToOrder(Long orderId, Long menuItemId, Integer quantity, String notes);
} 