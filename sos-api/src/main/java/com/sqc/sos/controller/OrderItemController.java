package com.sqc.sos.controller;

import com.sqc.sos.dto.ApiResponse;
import com.sqc.sos.dto.orderitem.OrderItemStatusResponse;
import com.sqc.sos.dto.orderitem.OrderItemResponse;
import com.sqc.sos.dto.orderitem.UpdateOrderItemStatusRequest;
import com.sqc.sos.model.OrderItemStatus;
import com.sqc.sos.service.IOrderItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/order-items")
@RequiredArgsConstructor
public class OrderItemController {
    
    private final IOrderItemService orderItemService;
    
    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<List<OrderItemStatusResponse>>> getOrderItemStatuses() {
        log.info("GET /order-items/statuses");
        List<OrderItemStatusResponse> statuses = orderItemService.getAllStatuses();
        return ResponseEntity.ok(ApiResponse.success(statuses, "Lấy danh sách trạng thái món thành công"));
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<OrderItemResponse>>> getOrderItemsByOrderId(@PathVariable Long orderId) {
        log.info("GET /order-items/order/{}", orderId);
        List<OrderItemResponse> orderItems = orderItemService.getOrderItemsByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(orderItems, "Lấy danh sách món trong đơn hàng thành công"));
    }
    
    @GetMapping("/{orderItemId}")
    public ResponseEntity<ApiResponse<OrderItemResponse>> getOrderItemById(@PathVariable Long orderItemId) {
        log.info("GET /order-items/{}", orderItemId);
        OrderItemResponse orderItem = orderItemService.getOrderItemById(orderItemId);
        return ResponseEntity.ok(ApiResponse.success(orderItem, "Lấy thông tin món thành công"));
    }
    
    @GetMapping("/pending-for-management")
    public ResponseEntity<ApiResponse<List<OrderItemResponse>>> getPendingOrderItemsForManagement() {
        log.info("GET /order-items/pending-for-management");
        List<OrderItemResponse> pendingItems = orderItemService.getPendingOrderItemsForManagement();
        return ResponseEntity.ok(ApiResponse.success(pendingItems, "Lấy danh sách món cần quản lý trạng thái thành công"));
    }
    
    @PatchMapping("/{orderItemId}/status")
    public ResponseEntity<ApiResponse<Void>> updateOrderItemStatus(
            @PathVariable Long orderItemId,
            @RequestBody UpdateOrderItemStatusRequest request) {
        log.info("PATCH /order-items/{}/status - status: {}", orderItemId, request.getStatus());
        orderItemService.updateStatus(orderItemId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(null, "Cập nhật trạng thái món thành công"));
    }
    
    @PatchMapping("/{orderItemId}/status/quick")
    public ResponseEntity<ApiResponse<Void>> updateOrderItemStatusQuick(
            @PathVariable Long orderItemId,
            @RequestParam OrderItemStatus status) {
        log.info("PATCH /order-items/{}/status/quick - status: {}", orderItemId, status);
        orderItemService.updateStatus(orderItemId, status);
        return ResponseEntity.ok(ApiResponse.success(null, "Cập nhật trạng thái món thành công"));
    }
    
    @PatchMapping("/{orderItemId}/status/partial")
    public ResponseEntity<ApiResponse<Void>> updateOrderItemStatusPartial(
            @PathVariable Long orderItemId,
            @RequestParam OrderItemStatus status,
            @RequestParam Integer quantity) {
        log.info("PATCH /order-items/{}/status/partial - status: {}, quantity: {}", orderItemId, status, quantity);
        orderItemService.updateStatusPartial(orderItemId, status, quantity);
        return ResponseEntity.ok(ApiResponse.success(null, "Cập nhật trạng thái món theo số lượng thành công"));
    }
    
    @PatchMapping("/batch/status/quick")
    public ResponseEntity<ApiResponse<Void>> updateMultipleOrderItemsStatusQuick(
            @RequestParam List<Long> orderItemIds,
            @RequestParam OrderItemStatus status) {
        log.info("PATCH /order-items/batch/status/quick - orderItemIds: {}, status: {}", orderItemIds, status);
        orderItemService.updateMultipleStatuses(orderItemIds, status);
        return ResponseEntity.ok(ApiResponse.success(null, "Cập nhật trạng thái nhiều món thành công"));
    }

    @PostMapping("/order/{orderId}/add-item")
    public ResponseEntity<ApiResponse<OrderItemResponse>> addItemToOrder(
            @PathVariable Long orderId,
            @RequestParam Long menuItemId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String notes) {
        log.info("POST /order-items/order/{}/add-item - menuItemId: {}, quantity: {}, notes: {}", 
                orderId, menuItemId, quantity, notes);
        
        OrderItemResponse orderItem = orderItemService.addItemToOrder(orderId, menuItemId, quantity, notes);
        return ResponseEntity.ok(ApiResponse.success(orderItem, "Thêm món vào đơn hàng thành công"));
    }

    @PostMapping("/order/{orderId}/merge-duplicates")
    public ResponseEntity<ApiResponse<Void>> mergeDuplicateOrderItems(@PathVariable Long orderId) {
        log.info("POST /order-items/order/{}/merge-duplicates", orderId);
        orderItemService.mergeDuplicateOrderItems(orderId);
        return ResponseEntity.ok(ApiResponse.success(null, "Merge các món trùng lặp thành công"));
    }
} 