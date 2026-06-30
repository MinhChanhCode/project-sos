package com.sqc.sos.service;

import com.sqc.sos.dto.orderitem.OrderItemStatusResponse;
import com.sqc.sos.dto.orderitem.OrderItemResponse;
import com.sqc.sos.dto.orderitem.UpdateOrderItemStatusRequest;
import com.sqc.sos.model.OrderItem;
import com.sqc.sos.model.OrderItemStatus;
import com.sqc.sos.model.Order;
import com.sqc.sos.model.MenuItem;
import com.sqc.sos.repository.IMenuItemRepository;
import com.sqc.sos.repository.IOrderItemRepository;
import com.sqc.sos.repository.IOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderItemService implements IOrderItemService {
    
    private final IOrderItemRepository orderItemRepository;
    private final IOrderRepository orderRepository;
    private final IMenuItemRepository menuItemRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    @Override
    public List<OrderItemStatusResponse> getAllStatuses() {
        return Arrays.stream(OrderItemStatus.values())
                .map(status -> new OrderItemStatusResponse(
                        status.name(),
                        getStatusDisplayName(status)
                ))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void updateStatus(Long orderItemId, OrderItemStatus status) {
        log.info("Updating order item {} status to {}", orderItemId, status);
        
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("Order item not found with id: " + orderItemId));

        // Không lưu 'status' nữa, chỉ thao tác theo các cột số lượng
        switch (status) {
            case PENDING: {
                int moved = orderItem.getPreparingQuantity();
                orderItem.setPreparingQuantity(0);
                orderItem.setPendingQuantity(orderItem.getPendingQuantity() + moved);
                break;
            }
            case PREPARING: {
                int moved = orderItem.getPendingQuantity();
                orderItem.setPendingQuantity(0);
                orderItem.setPreparingQuantity(orderItem.getPreparingQuantity() + moved);
                break;
            }
            case COMPLETED: {
                // Theo từng cấp: chỉ lấy từ PREPARING
                int moved = orderItem.getPreparingQuantity();
                orderItem.setPreparingQuantity(0);
                orderItem.setCompletedQuantity(orderItem.getCompletedQuantity() + moved);
                break;
            }
            case SERVED: {
                int moved = orderItem.getCompletedQuantity();
                orderItem.setCompletedQuantity(0);
                orderItem.setServedQuantity(orderItem.getServedQuantity() + moved);
                break;
            }
            case CANCELLED: {
                // Chỉ từ PENDING
                int moved = orderItem.getPendingQuantity();
                orderItem.setPendingQuantity(0);
                orderItem.setCancelledQuantity(orderItem.getCancelledQuantity() + moved);
                break;
            }
            case OUT_OF_STOCK: {
                // Chỉ từ PENDING
                int moved = orderItem.getPendingQuantity();
                orderItem.setPendingQuantity(0);
                orderItem.setOutOfStockQuantity(orderItem.getOutOfStockQuantity() + moved);
                break;
            }
        }
        
        orderItemRepository.save(orderItem);
        
        // Đảm bảo không có duplicate sau khi cập nhật
        if (orderItem.getOrder() != null) {
            mergeDuplicateOrderItems(orderItem.getOrder().getId());
        }
        
        log.info("Order item {} status updated to {}", orderItemId, status);

        // Phát sự kiện real-time và thông báo
        eventPublisher.publishEvent(new OrderItemStatusChangedEvent(orderItem));
    }

    @Override
    @Transactional
    public void updateStatusPartial(Long orderItemId, OrderItemStatus status, Integer quantity) {
        log.info("Updating order item {} status to {} for quantity {}", orderItemId, status, quantity);
        
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("Order item not found with id: " + orderItemId));

        // Validate quantity theo trạng thái đích
        int available;
        if (status == OrderItemStatus.SERVED) {
            available = orderItem.getCompletedQuantity();
        } else if (status == OrderItemStatus.COMPLETED) {
            // Chỉ được hoàn tất từ PREPARING
            available = orderItem.getPreparingQuantity();
        } else if (status == OrderItemStatus.CANCELLED || status == OrderItemStatus.OUT_OF_STOCK) {
            // Chỉ hủy/hết món từ PENDING
            available = orderItem.getPendingQuantity();
        } else if (status == OrderItemStatus.PREPARING) {
            available = orderItem.getPendingQuantity();
        } else if (status == OrderItemStatus.PENDING) {
            available = orderItem.getPreparingQuantity();
        } else {
            available = orderItem.getPendingQuantity() + orderItem.getPreparingQuantity();
        }
        if (quantity <= 0 || quantity > available) {
            throw new RuntimeException("Invalid quantity: " + quantity + " for order item with available: " + available);
        }

        // Không còn trạng thái lưu trong DB; validate dựa trên số lượng khả dụng đã tính ở trên

        // Nếu cập nhật toàn bộ số lượng, cập nhật trạng thái
        if (quantity.equals(available)) {
            // Di chuyển toàn bộ 'available' sang trạng thái mới theo nguồn đúng
            if (status == OrderItemStatus.SERVED) {
                moveFromCompleted(orderItem, available);
            } else {
                moveFromPendingPreparing(orderItem, status, available);
            }
            orderItemRepository.save(orderItem);
        } else {
            // Nếu cập nhật một phần: chỉ điều chỉnh trên CHÍNH orderItem, không tạo record mới
            if (status == OrderItemStatus.SERVED) {
                // Lấy từ completed -> served
                moveFromCompleted(orderItem, quantity);
            } else if (status == OrderItemStatus.COMPLETED) {
                // Chỉ từ PREPARING
                int fromPreparing = Math.min(quantity, orderItem.getPreparingQuantity());
                orderItem.setPreparingQuantity(orderItem.getPreparingQuantity() - fromPreparing);
                orderItem.setCompletedQuantity(orderItem.getCompletedQuantity() + fromPreparing);
            } else {
                // Các trạng thái còn lại di chuyển đúng một bậc
                moveFromPendingPreparing(orderItem, status, quantity);
            }
            orderItemRepository.save(orderItem);
        }
        
        // Đảm bảo không có duplicate sau khi cập nhật
        if (orderItem.getOrder() != null) {
            mergeDuplicateOrderItems(orderItem.getOrder().getId());
        }
        
        log.info("Order item {} status partially updated to {} for quantity {}", orderItemId, status, quantity);

        // Phát sự kiện real-time cho order item gốc
        eventPublisher.publishEvent(new OrderItemStatusChangedEvent(orderItem));
    }

    private void moveFromPendingPreparing(OrderItem orderItem, OrderItemStatus target, int quantity) {
        int fromPending;
        int fromPreparing;
        int remaining;

        if (target == OrderItemStatus.COMPLETED) {
            // Chỉ từ PREPARING
            fromPreparing = Math.min(quantity, orderItem.getPreparingQuantity());
            orderItem.setPreparingQuantity(orderItem.getPreparingQuantity() - fromPreparing);
            remaining = quantity - fromPreparing; // phần còn lại bỏ qua (không lấy từ pending)
            fromPending = 0;
        } else if (target == OrderItemStatus.PREPARING) {
            fromPending = Math.min(quantity, orderItem.getPendingQuantity());
            orderItem.setPendingQuantity(orderItem.getPendingQuantity() - fromPending);
            remaining = quantity - fromPending;
            fromPreparing = 0; // không lấy ngược từ preparing
        } else if (target == OrderItemStatus.PENDING) {
            fromPreparing = Math.min(quantity, orderItem.getPreparingQuantity());
            orderItem.setPreparingQuantity(orderItem.getPreparingQuantity() - fromPreparing);
            remaining = quantity - fromPreparing;
            fromPending = 0; // không lấy ngược từ pending
        } else if (target == OrderItemStatus.CANCELLED || target == OrderItemStatus.OUT_OF_STOCK) {
            fromPending = Math.min(quantity, orderItem.getPendingQuantity());
            orderItem.setPendingQuantity(orderItem.getPendingQuantity() - fromPending);
            remaining = quantity - fromPending;
            fromPreparing = 0; // không lấy từ preparing
        } else {
            // default (không nên xảy ra)
            fromPending = Math.min(quantity, orderItem.getPendingQuantity());
            orderItem.setPendingQuantity(orderItem.getPendingQuantity() - fromPending);
            remaining = quantity - fromPending;
            fromPreparing = Math.min(remaining, orderItem.getPreparingQuantity());
            orderItem.setPreparingQuantity(orderItem.getPreparingQuantity() - fromPreparing);
        }

        int moved = fromPending + fromPreparing;
        switch (target) {
            case PENDING: orderItem.setPendingQuantity(orderItem.getPendingQuantity() + moved); break;
            case PREPARING: orderItem.setPreparingQuantity(orderItem.getPreparingQuantity() + moved); break;
            case COMPLETED: orderItem.setCompletedQuantity(orderItem.getCompletedQuantity() + moved); break;
            case CANCELLED: orderItem.setCancelledQuantity(orderItem.getCancelledQuantity() + moved); break;
            case OUT_OF_STOCK: orderItem.setOutOfStockQuantity(orderItem.getOutOfStockQuantity() + moved); break;
            default: break;
        }
    }

    private void moveFromCompleted(OrderItem orderItem, int quantity) {
        int fromCompleted = Math.min(quantity, orderItem.getCompletedQuantity());
        orderItem.setCompletedQuantity(orderItem.getCompletedQuantity() - fromCompleted);
        orderItem.setServedQuantity(orderItem.getServedQuantity() + fromCompleted);
    }

    @Override
    @Transactional
    public void updateMultipleStatuses(List<Long> orderItemIds, OrderItemStatus status) {
        log.info("Updating multiple order items {} status to {}", orderItemIds, status);
        
        List<OrderItem> orderItems = orderItemRepository.findAllById(orderItemIds);
        
        if (orderItems.size() != orderItemIds.size()) {
            throw new RuntimeException("Some order items not found");
        }
        
        for (OrderItem orderItem : orderItems) {
            switch (status) {
                case PENDING: {
                    int moved = orderItem.getPreparingQuantity();
                    orderItem.setPreparingQuantity(0);
                    orderItem.setPendingQuantity(orderItem.getPendingQuantity() + moved);
                    break;
                }
                case PREPARING: {
                    int moved = orderItem.getPendingQuantity();
                    orderItem.setPendingQuantity(0);
                    orderItem.setPreparingQuantity(orderItem.getPreparingQuantity() + moved);
                    break;
                }
                case COMPLETED: {
                    int moved = orderItem.getPendingQuantity() + orderItem.getPreparingQuantity();
                    orderItem.setPendingQuantity(0);
                    orderItem.setPreparingQuantity(0);
                    orderItem.setCompletedQuantity(orderItem.getCompletedQuantity() + moved);
                    break;
                }
                case SERVED: {
                    int moved = orderItem.getCompletedQuantity();
                    orderItem.setCompletedQuantity(0);
                    orderItem.setServedQuantity(orderItem.getServedQuantity() + moved);
                    break;
                }
                case CANCELLED: {
                    int moved = orderItem.getPendingQuantity() + orderItem.getPreparingQuantity();
                    orderItem.setPendingQuantity(0);
                    orderItem.setPreparingQuantity(0);
                    orderItem.setCancelledQuantity(orderItem.getCancelledQuantity() + moved);
                    break;
                }
                case OUT_OF_STOCK: {
                    int moved = orderItem.getPendingQuantity() + orderItem.getPreparingQuantity();
                    orderItem.setPendingQuantity(0);
                    orderItem.setPreparingQuantity(0);
                    orderItem.setOutOfStockQuantity(orderItem.getOutOfStockQuantity() + moved);
                    break;
                }
            }
        }
        
        orderItemRepository.saveAll(orderItems);
        
        // Đảm bảo không có duplicate sau khi cập nhật batch
        if (!orderItems.isEmpty() && orderItems.get(0).getOrder() != null) {
            mergeDuplicateOrderItems(orderItems.get(0).getOrder().getId());
        }
        
        log.info("Multiple order items {} status updated to {}", orderItemIds, status);

        // Phát sự kiện real-time cho từng order item
        for (OrderItem orderItem : orderItems) {
            eventPublisher.publishEvent(new OrderItemStatusChangedEvent(orderItem));
        }
    }

    // Sự kiện domain để phát ra cho WebSocket/SSE hoặc NotificationService
    public static class OrderItemStatusChangedEvent {
        private final OrderItem orderItem;
        public OrderItemStatusChangedEvent(OrderItem orderItem) { this.orderItem = orderItem; }
        public OrderItem getOrderItem() { return orderItem; }
    }
    
    @Override
    public List<OrderItemResponse> getOrderItemsByOrderId(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return orderItems.stream()
                .map(this::mapToOrderItemResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public OrderItemResponse getOrderItemById(Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("Order item not found with id: " + orderItemId));
        return mapToOrderItemResponse(orderItem);
    }
    
    @Override
    public List<OrderItemResponse> getPendingOrderItemsForManagement() {
        // Lấy tất cả các món trong trạng thái cần quản lý (chỉ PENDING & PREPARING)
        List<OrderItem> pendingItems = orderItemRepository.findManagementCandidates();
        
        return pendingItems.stream()
                .map(this::mapToOrderItemResponseWithOrderInfo)
                .collect(Collectors.toList());
    }
    
    private OrderItemResponse mapToOrderItemResponseWithOrderInfo(OrderItem orderItem) {
        OrderItemResponse response = mapToOrderItemResponse(orderItem);
        
        // Thêm thông tin bàn và đơn hàng
        if (orderItem.getOrder() != null) {
            response.setOrderId(orderItem.getOrder().getId());
            response.setOrderTime(orderItem.getOrder().getCreatedAt());
            if (orderItem.getOrder().getTable() != null) {
                response.setTableName(orderItem.getOrder().getTable().getName());
            }
        }
        
        return response;
    }
    
    private OrderItemResponse mapToOrderItemResponse(OrderItem orderItem) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(orderItem.getId());
        response.setMenuItemId(orderItem.getMenuItem().getId());
        response.setMenuItemName(orderItem.getMenuItem().getName());
        response.setPendingQuantity(orderItem.getPendingQuantity());
        response.setPreparingQuantity(orderItem.getPreparingQuantity());
        response.setCompletedQuantity(orderItem.getCompletedQuantity());
        response.setServedQuantity(orderItem.getServedQuantity());
        response.setCancelledQuantity(orderItem.getCancelledQuantity());
        response.setOutOfStockQuantity(orderItem.getOutOfStockQuantity());
        int effective = orderItem.getEffectiveQuantity();
        response.setTotalQuantity(effective);
        response.setUnitPrice(orderItem.getMenuItem().getPrice());
        response.setTotalPrice(orderItem.getMenuItem().getPrice().multiply(new java.math.BigDecimal(effective)));
        // Không còn status trong response
        response.setNotes(orderItem.getNotes()); // Thêm ghi chú
        
        // Thêm thời gian order
        if (orderItem.getOrder() != null) {
            response.setOrderTime(orderItem.getOrder().getCreatedAt());
        }
        
        return response;
    }
    
    // Dùng để hiển thị tên trạng thái (phục vụ API /statuses)
    private String getStatusDisplayName(OrderItemStatus status) {
        switch (status) {
            case PENDING: return "Chờ xử lý";
            case PREPARING: return "Đang chế biến";
            case COMPLETED: return "Hoàn tất";
            case SERVED: return "Đã phục vụ";
            case CANCELLED: return "Đã hủy";
            case OUT_OF_STOCK: return "Hết món";
            default: return status.name();
        }
    }

    /**
     * Merge các order items duplicate trong cùng một order
     * Đây là method để xử lý trường hợp có nhiều order items duplicate
     */
    public void mergeDuplicateOrderItems(Long orderId) {
        // Lấy tất cả order items trong order
        List<OrderItem> allItems = orderItemRepository.findByOrderId(orderId);
        
        // Group theo menuItemId
        Map<Long, List<OrderItem>> groupedItems = allItems.stream()
                .collect(Collectors.groupingBy(item -> item.getMenuItem().getId()));
        
        // Xử lý từng group
        for (Map.Entry<Long, List<OrderItem>> entry : groupedItems.entrySet()) {
            List<OrderItem> items = entry.getValue();
            if (items.size() > 1) {
                // Có duplicate, merge vào item đầu tiên
                OrderItem mainItem = items.get(0);
                int totalPending = items.stream().mapToInt(i -> i.getPendingQuantity() == null ? 0 : i.getPendingQuantity()).sum();
                int totalPreparing = items.stream().mapToInt(i -> i.getPreparingQuantity() == null ? 0 : i.getPreparingQuantity()).sum();
                int totalCompletedQuantity = items.stream().mapToInt(i -> i.getCompletedQuantity() == null ? 0 : i.getCompletedQuantity()).sum();
                int totalServed = items.stream().mapToInt(i -> i.getServedQuantity() == null ? 0 : i.getServedQuantity()).sum();
                int totalCancelled = items.stream().mapToInt(i -> i.getCancelledQuantity() == null ? 0 : i.getCancelledQuantity()).sum();
                int totalOutOfStock = items.stream().mapToInt(i -> i.getOutOfStockQuantity() == null ? 0 : i.getOutOfStockQuantity()).sum();
                
                // Merge ghi chú
                String mergedNotes = items.stream()
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

                orderItemRepository.save(mainItem);
                
                // Xóa các item còn lại
                for (int i = 1; i < items.size(); i++) {
                    orderItemRepository.delete(items.get(i));
                }
                
                log.info("Merged {} duplicate order items for menu item {} in order {}", 
                        items.size(), mainItem.getMenuItem().getName(), orderId);
            }
        }
    }

    /**
     * Thêm món mới vào order với logic merge tự động
     * Nếu đã có món này trong order, sẽ cộng dồn số lượng thay vì tạo record mới
     */
    public OrderItemResponse addItemToOrder(Long orderId, Long menuItemId, Integer quantity, String notes) {
        // Kiểm tra xem đã có order item cho món này trong order chưa
        List<OrderItem> existingItems = orderItemRepository.findByOrderId(orderId);
        Optional<OrderItem> existingItem = existingItems.stream()
                .filter(item -> item.getMenuItem().getId().equals(menuItemId))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Nếu đã có, cộng dồn số lượng PENDING
            OrderItem item = existingItem.get();
            item.setPendingQuantity(item.getPendingQuantity() + quantity);
            
            // Merge ghi chú nếu có
            if (notes != null && !notes.trim().isEmpty()) {
                String existingNotes = item.getNotes();
                if (existingNotes != null && !existingNotes.trim().isEmpty()) {
                    item.setNotes(existingNotes + "; " + notes);
                } else {
                    item.setNotes(notes);
                }
            }
            
            orderItemRepository.save(item);
            log.info("Updated existing order item {} with quantity {} for menu item {} in order {}", 
                    item.getId(), item.getEffectiveQuantity(), menuItemId, orderId);
            
            return mapToOrderItemResponse(item);
        } else {
            // Nếu chưa có, tạo mới
            // Lấy thông tin order và menu item
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
            MenuItem menuItem = menuItemRepository.findById(menuItemId)
                    .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + menuItemId));
            
            OrderItem newItem = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .pendingQuantity(quantity)
                    .completedQuantity(0)
                    .notes(notes)
                    .build();
            
            OrderItem savedItem = orderItemRepository.save(newItem);
            log.info("Created new order item {} with quantity {} for menu item {} in order {}", 
                    savedItem.getId(), quantity, menuItemId, orderId);
            
            return mapToOrderItemResponse(savedItem);
        }
    }
}