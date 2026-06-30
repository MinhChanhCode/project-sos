package com.sqc.sos.controller;

import com.sqc.sos.dto.ApiResponse;
import com.sqc.sos.model.Order;
import com.sqc.sos.repository.IOrderRepository;
import com.sqc.sos.service.OrderWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderWorkflowService orderWorkflowService;
    private final IOrderRepository orderRepository;

    @PostMapping("/session/{sessionId}/confirm")
    public ResponseEntity<ApiResponse<Long>> confirmOrder(@PathVariable String sessionId) {
        log.info("POST /orders/session/{}/confirm", sessionId);
        Long orderId = orderWorkflowService.confirmOrder(sessionId);
        return ResponseEntity.ok(ApiResponse.success(orderId, "Tạo đơn hàng thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listOrders() {
        List<Map<String, Object>> orders = orderRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toSummary)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(orders, "Lấy danh sách đơn thành công"));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new com.sqc.sos.exception.AppException(com.sqc.sos.exception.ErrorCode.ORDER_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success(toSummary(order), "Lấy đơn hàng thành công"));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancelOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new com.sqc.sos.exception.AppException(com.sqc.sos.exception.ErrorCode.ORDER_NOT_FOUND));
        order.setStatus("CANCELLED");
        orderRepository.save(order);
        return ResponseEntity.ok(ApiResponse.success(toSummary(order), "Hủy đơn thành công"));
    }

    private Map<String, Object> toSummary(Order order) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("status", order.getStatus());
        map.put("createdAt", order.getCreatedAt());
        map.put("completedAt", order.getCompletedAt());
        if (order.getTable() != null) {
            map.put("tableId", order.getTable().getId());
            map.put("tableName", order.getTable().getName());
        }
        return map;
    }
}
