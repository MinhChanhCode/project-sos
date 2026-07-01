package com.sqc.sos.service;

import com.sqc.sos.model.OrderItem;
import com.sqc.sos.dto.orderitem.OrderItemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import com.sqc.sos.service.CartUpdatedEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealtimeService {

    private final SimpMessagingTemplate messagingTemplate;
    private final OrderItemService orderItemService;

    @EventListener
    public void onOrderItemStatusChanged(OrderItemService.OrderItemStatusChangedEvent event) {
        OrderItem item = event.getOrderItem();

        Map<String, Object> payload = new HashMap<>();
        payload.put("orderItemId", item.getId());
        payload.put("orderId", item.getOrder() != null ? item.getOrder().getId() : null);
        payload.put("menuItemId", item.getMenuItem() != null ? item.getMenuItem().getId() : null);
        payload.put("menuItemName", item.getMenuItem() != null ? item.getMenuItem().getName() : null);
        if (item.getOrder() != null && item.getOrder().getTable() != null) {
            payload.put("tableId", item.getOrder().getTable().getId());
            payload.put("tableName", item.getOrder().getTable().getName());
        }

        Long orderId = item.getOrder() != null ? item.getOrder().getId() : null;
        if (orderId != null) {
            String destination = "/topic/orders/" + orderId;
            messagingTemplate.convertAndSend(destination, payload);
            log.info("Sent realtime update to {}: {}", destination, payload);
        }

        // Gửi thông báo đến tất cả khách cùng bàn khi trạng thái món thay đổi
        if (item.getOrder() != null && item.getOrder().getTable() != null) {
            UUID tableId = item.getOrder().getTable().getId();
            
            // Thêm type để frontend dễ xử lý
            Map<String, Object> tablePayload = new HashMap<>(payload);
            tablePayload.put("type", "ORDER_ITEM_STATUS_CHANGED");
            // Include per-status quantities for FE merge without refetch
            tablePayload.put("pendingQuantity", item.getPendingQuantity());
            tablePayload.put("preparingQuantity", item.getPreparingQuantity());
            tablePayload.put("completedQuantity", item.getCompletedQuantity());
            tablePayload.put("servedQuantity", item.getServedQuantity());
            tablePayload.put("cancelledQuantity", item.getCancelledQuantity());
            tablePayload.put("outOfStockQuantity", item.getOutOfStockQuantity());
            if (item.getMenuItem() != null) {
                tablePayload.put("unitPrice", item.getMenuItem().getPrice());
            }
            
            // Gửi đến topic của bàn để tất cả khách cùng bàn đều nhận được
            String tableDestination = "/topic/tables/" + tableId + "/order-items";
            messagingTemplate.convertAndSend(tableDestination, tablePayload);
            log.info("Sent table order item status update to {}: {}", tableDestination, tablePayload);
            
            // Gửi thông báo chung cho bàn
            Map<String, Object> generalPayload = new HashMap<>();
            generalPayload.put("type", "ORDER_STATUS_UPDATED");
            generalPayload.put("message", "Trạng thái món đã được cập nhật");
            generalPayload.put("orderItemId", item.getId());
            
            String generalDestination = "/topic/tables/" + tableId;
            messagingTemplate.convertAndSend(generalDestination, generalPayload);
            log.info("Sent general table update to {}: {}", generalDestination, generalPayload);
        }

        // Phát cho kênh management với đầy đủ payload OrderItemResponse để FE hợp nhất không cần refetch
        try {
            OrderItemResponse itemResponse = orderItemService.getOrderItemById(item.getId());
            Map<String, Object> itemMap = new HashMap<>();
            // Basic fields from DTO
            itemMap.put("id", itemResponse.getId());
            itemMap.put("menuItemId", itemResponse.getMenuItemId());
            itemMap.put("menuItemName", itemResponse.getMenuItemName());
            itemMap.put("pendingQuantity", itemResponse.getPendingQuantity());
            itemMap.put("preparingQuantity", itemResponse.getPreparingQuantity());
            itemMap.put("completedQuantity", itemResponse.getCompletedQuantity());
            itemMap.put("servedQuantity", itemResponse.getServedQuantity());
            itemMap.put("cancelledQuantity", itemResponse.getCancelledQuantity());
            itemMap.put("outOfStockQuantity", itemResponse.getOutOfStockQuantity());
            itemMap.put("totalQuantity", itemResponse.getTotalQuantity());
            itemMap.put("unitPrice", itemResponse.getUnitPrice());
            itemMap.put("totalPrice", itemResponse.getTotalPrice());
            itemMap.put("orderId", itemResponse.getOrderId());
            itemMap.put("notes", itemResponse.getNotes());
            // Enrich with table info
            if (item.getOrder() != null && item.getOrder().getTable() != null) {
                itemMap.put("tableName", item.getOrder().getTable().getName());
            }
            // Optional: order time if available
            // itemMap.put("orderTime", ...);

            Map<String, Object> managementPayload = new HashMap<>();
            managementPayload.put("type", "ORDER_ITEM_STATUS_CHANGED");
            managementPayload.put("item", itemMap);
            messagingTemplate.convertAndSend("/topic/management/order-items", managementPayload);
        } catch (Exception e) {
            log.warn("Failed to send management payload for order item {}", item.getId(), e);
        }

        // Nếu món đã COMPLETED (dựa trên quantity), phát thêm kênh dành cho nhân viên phục vụ để biết món nào đã xong ở bàn nào
        if (item.getCompletedQuantity() != null && item.getCompletedQuantity() > 0) {
            String serverDest = "/topic/server/orders-ready";
            messagingTemplate.convertAndSend(serverDest, payload);
            log.info("Sent server READY notification: {} -> {}", serverDest, payload);
        }
    }

    @EventListener
    public void onOrderCreated(OrderWorkflowService.OrderCreatedEvent event) {
        Long orderId = event.getOrderId();
        if (orderId == null) return;

        // Thông báo cho bếp có đơn hàng mới cần xử lý
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "ORDER_CREATED");
        payload.put("orderId", orderId);
        payload.put("tableId", event.getTableId());
        payload.put("tableName", event.getTableName());
        String destination = "/topic/kitchen/orders";
        messagingTemplate.convertAndSend(destination, payload);
        log.info("Sent realtime ORDER_CREATED to {}: {}", destination, payload);

        // Also push items of this order to table-specific ordered topic
        try {
            java.util.List<OrderItemResponse> items = orderItemService.getOrderItemsByOrderId(orderId);
            Map<String, Object> tableOrderedPayload = new HashMap<>();
            tableOrderedPayload.put("type", "ORDERED_UPDATED");
            tableOrderedPayload.put("orderId", orderId);
            tableOrderedPayload.put("items", items);
            if (event.getTableId() != null) {
                String tableOrderedDest = "/topic/tables/" + event.getTableId() + "/ordered";
                messagingTemplate.convertAndSend(tableOrderedDest, tableOrderedPayload);
            }
        } catch (Exception e) {
            log.warn("Failed to send table ordered payload for order {}", orderId, e);
        }

        // Gửi cho kênh quản lý trạng thái: danh sách order items của order vừa tạo
        try {
            java.util.List<OrderItemResponse> items = orderItemService.getOrderItemsByOrderId(orderId);
            Map<String, Object> managementPayload = new HashMap<>();
            managementPayload.put("type", "ORDERED_UPDATED");
            managementPayload.put("orderId", orderId);
            managementPayload.put("items", items);
            messagingTemplate.convertAndSend("/topic/management/order-items", managementPayload);
        } catch (Exception e) {
            log.warn("Failed to send management ORDERED_UPDATED for order {}", orderId, e);
        }
    }

    @EventListener
    public void onMenuItemChanged(MenuItemChangedEvent event) {
        if (event.getItem() == null) return;
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "MENU_ITEM_CHANGED");
        payload.put("action", event.getAction());
        payload.put("item", event.getItem());
        messagingTemplate.convertAndSend("/topic/menu-items", payload);
        log.info("Sent menu item realtime update: {}", payload);
    }

    @EventListener
    public void onReviewCreated(ReviewCreatedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "REVIEW_CREATED");
        payload.put("review", event.getReview());
        messagingTemplate.convertAndSend("/topic/reviews", payload);
        log.info("Sent review realtime update: {}", payload);
    }

    @EventListener
    public void onTableCleared(TableQueryService.TableClearedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tableId", event.getTableId());
        payload.put("type", "TABLE_CLEARED");
        String destination = "/topic/tables/" + event.getTableId();
        messagingTemplate.convertAndSend(destination, payload);
        log.info("Sent realtime table cleared: {} -> {}", event.getTableId(), destination);
    }

    public void sendPaymentEvent(Map<String, Object> payload) {
        messagingTemplate.convertAndSend("/topic/payment", payload);
        log.info("Sent payment event: {}", payload);
    }
}
