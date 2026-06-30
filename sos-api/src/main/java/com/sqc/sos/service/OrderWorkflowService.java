package com.sqc.sos.service;

import com.sqc.sos.exception.AppException;
import com.sqc.sos.exception.ErrorCode;
import com.sqc.sos.model.*;
import com.sqc.sos.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderWorkflowService {

    private final ICartRepository cartRepository;
    private final ICartItemRepository cartItemRepository;
    private final IOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long confirmOrder(String sessionId) {
        Cart cart = cartRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        List<CartItem> cartItems = cartItemRepository.findByCartIdAndIsActiveTrue(cart.getId());
        if (cartItems.isEmpty()) throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);

        // Đảm bảo mỗi giỏ hàng chỉ tạo ra một Order duy nhất
        // Kiểm tra xem đã có Order nào cho giỏ hàng này chưa
        Order order = orderRepository.findByTableIdAndStatus(cart.getTable().getId(), "PENDING")
                .orElseGet(() -> {
                    // Nếu chưa có, tạo mới
                    Order newOrder = Order.builder()
                            .table(cart.getTable())
                            .status("PENDING")
                            .createdAt(LocalDateTime.now())
                            .build();
                    return orderRepository.save(newOrder);
                });

        // Map để theo dõi order items theo menuItemId để merge
        // Đảm bảo mỗi món chỉ có một OrderItem duy nhất với ID duy nhất
        Map<Long, OrderItem> orderItemMap = new HashMap<>();
        
        for (CartItem ci : cartItems) {
            Long menuItemId = ci.getMenuItem().getId();
            
            if (orderItemMap.containsKey(menuItemId)) {
                // Nếu đã có order item cho món này, cộng dồn số lượng PENDING
                // KHÔNG tạo OrderItem mới, chỉ cập nhật OrderItem hiện có
                OrderItem existingItem = orderItemMap.get(menuItemId);
                existingItem.setPendingQuantity(existingItem.getPendingQuantity() + ci.getQuantity());
                
                // Merge ghi chú nếu có
                if (ci.getNotes() != null && !ci.getNotes().trim().isEmpty()) {
                    String existingNotes = existingItem.getNotes();
                    if (existingNotes != null && !existingNotes.trim().isEmpty()) {
                        existingItem.setNotes(existingNotes + "; " + ci.getNotes());
                    } else {
                        existingItem.setNotes(ci.getNotes());
                    }
                }
                
                log.info("Merged cart item {} into existing order item {} for menu item {}", 
                        ci.getId(), existingItem.getId(), menuItemId);
            } else {
                // Tạo mới order item - chỉ tạo một lần duy nhất cho mỗi món
                OrderItem oi = OrderItem.builder()
                        .order(order)
                        .menuItem(ci.getMenuItem())
                        .pendingQuantity(ci.getQuantity())
                        .completedQuantity(0) // Khởi tạo số lượng hoàn thành = 0
                        .notes(ci.getNotes()) // Copy ghi chú từ cart item sang order item
                        .build();
                orderItemMap.put(menuItemId, oi);
                
                log.info("Created new order item {} for menu item {} with quantity {}", 
                        oi.getId(), menuItemId, ci.getQuantity());
            }
            
            // Đánh dấu CartItem là không còn hiệu lực
            ci.setIsActive(false);
            cartItemRepository.save(ci);
        }
        
        // Lưu tất cả order items đã merge
        // Mỗi món sẽ chỉ có một OrderItem với ID duy nhất
        for (OrderItem oi : orderItemMap.values()) {
            orderItemRepository.save(oi);
        }
        
        log.info("Order {} created with {} unique order items (merged from {} cart items)", 
                order.getId(), orderItemMap.size(), cartItems.size());

        // Kiểm tra và đảm bảo không có OrderItem duplicate
        validateOrderItemUniqueness(order.getId());

        // Giữ cart hoạt động để khách tiếp tục order lần nữa
        // Không xóa các items hiện tại trong giỏ sau khi đã tạo đơn nữa
        // cartItemRepository.deleteByCartId(cart.getId());
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        // Phát sự kiện để realtime thông báo cho bếp có đơn mới
        eventPublisher.publishEvent(new OrderCreatedEvent(order.getId(), order.getTable().getId(), order.getTable().getName()));
        // Phát sự kiện realtime cập nhật ordered items cho các khách cùng bàn
        eventPublisher.publishEvent(new OrderedItemsUpdatedEvent(order.getTable().getId()));

        return order.getId();
    }

    /**
     * Kiểm tra và đảm bảo không có OrderItem duplicate trong Order
     * Mỗi món chỉ được phép có một OrderItem duy nhất
     */
    private void validateOrderItemUniqueness(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        
        // Group theo menuItemId để kiểm tra duplicate
        Map<Long, List<OrderItem>> groupedItems = orderItems.stream()
                .collect(Collectors.groupingBy(item -> item.getMenuItem().getId()));
        
        // Kiểm tra xem có món nào có nhiều OrderItem không
        for (Map.Entry<Long, List<OrderItem>> entry : groupedItems.entrySet()) {
            Long menuItemId = entry.getKey();
            List<OrderItem> items = entry.getValue();
            
            if (items.size() > 1) {
                log.warn("Found {} duplicate order items for menu item {} in order {}", 
                        items.size(), menuItemId, orderId);
                
                // Nếu có duplicate, merge chúng lại
                mergeDuplicateOrderItemsInOrder(orderId, menuItemId, items);
            }
        }
    }

    /**
     * Merge các OrderItem duplicate cho cùng một món trong Order
     */
    private void mergeDuplicateOrderItemsInOrder(Long orderId, Long menuItemId, List<OrderItem> duplicateItems) {
        if (duplicateItems.size() <= 1) return;
        
        // Giữ lại OrderItem đầu tiên, merge các OrderItem còn lại vào
        OrderItem mainItem = duplicateItems.get(0);
        
        // Cộng dồn số lượng theo từng trạng thái
        int totalPending = duplicateItems.stream().mapToInt(i -> i.getPendingQuantity() == null ? 0 : i.getPendingQuantity()).sum();
        int totalPreparing = duplicateItems.stream().mapToInt(i -> i.getPreparingQuantity() == null ? 0 : i.getPreparingQuantity()).sum();
        int totalCompletedQuantity = duplicateItems.stream().mapToInt(i -> i.getCompletedQuantity() == null ? 0 : i.getCompletedQuantity()).sum();
        int totalServed = duplicateItems.stream().mapToInt(i -> i.getServedQuantity() == null ? 0 : i.getServedQuantity()).sum();
        int totalCancelled = duplicateItems.stream().mapToInt(i -> i.getCancelledQuantity() == null ? 0 : i.getCancelledQuantity()).sum();
        int totalOutOfStock = duplicateItems.stream().mapToInt(i -> i.getOutOfStockQuantity() == null ? 0 : i.getOutOfStockQuantity()).sum();
        
        // Merge ghi chú
        String mergedNotes = duplicateItems.stream()
                .map(OrderItem::getNotes)
                .filter(notes -> notes != null && !notes.trim().isEmpty())
                .distinct()
                .collect(Collectors.joining("; "));
        
        mainItem.setPendingQuantity(totalPending);
        mainItem.setPreparingQuantity(totalPreparing);
        mainItem.setCompletedQuantity(totalCompletedQuantity);
        mainItem.setServedQuantity(totalServed);
        mainItem.setCancelledQuantity(totalCancelled);
        mainItem.setOutOfStockQuantity(totalOutOfStock);
        if (!mergedNotes.isEmpty()) {
            mainItem.setNotes(mergedNotes);
        }
        
        // Lưu OrderItem chính
        orderItemRepository.save(mainItem);
        
        // Xóa các OrderItem duplicate
        for (int i = 1; i < duplicateItems.size(); i++) {
            orderItemRepository.delete(duplicateItems.get(i));
        }
        
        log.info("Merged {} duplicate order items for menu item {} in order {} into single item with ID {}", 
                duplicateItems.size(), menuItemId, orderId, mainItem.getId());
    }

    // Sự kiện domain khi tạo đơn mới thành công
    @lombok.Value
    public static class OrderCreatedEvent {
        Long orderId;
        java.util.UUID tableId;
        String tableName;
    }
}


