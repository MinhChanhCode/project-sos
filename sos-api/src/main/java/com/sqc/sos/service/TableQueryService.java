package com.sqc.sos.service;

import com.sqc.sos.dto.table.TableDetailResponse;
import com.sqc.sos.dto.table.TableListItemResponse;
import com.sqc.sos.dto.table.TableOrderItemSummary;
import com.sqc.sos.exception.AppException;
import com.sqc.sos.exception.ErrorCode;
import com.sqc.sos.model.Order;
import com.sqc.sos.model.OrderItem;
import com.sqc.sos.model.TableEntity;
import com.sqc.sos.repository.IOrderItemRepository;
import com.sqc.sos.repository.ICartRepository;
import com.sqc.sos.repository.ICartItemRepository;
import com.sqc.sos.repository.IOrderRepository;
import com.sqc.sos.repository.ITableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import com.sqc.sos.service.TableStatusChangedEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TableQueryService {
    private static final int MAX_STANDARD_TABLES = 20;
    private static final int[][] DEFAULT_TABLE_POSITIONS = {
            {105, 74}, {275, 74}, {610, 74}, {760, 74}, {1045, 74},
            {1215, 74}, {160, 310}, {160, 455}, {600, 315}, {760, 315},
            {600, 475}, {760, 475}, {930, 315}, {930, 475}, {1160, 315},
            {1160, 475}, {290, 720}, {450, 720}, {850, 720}, {1010, 720}
    };

    private final ITableRepository tableRepository;
    private final IOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final ICartRepository cartRepository;
    private final ICartItemRepository cartItemRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<TableListItemResponse> listAllTables() {
        Map<Integer, TableEntity> standardTables = tableRepository.findAll().stream()
                .sorted(Comparator.comparing(table -> table.getId().toString()))
                .filter(table -> getStandardTableNumber(table.getName()) != null)
                .collect(Collectors.toMap(
                        table -> getStandardTableNumber(table.getName()),
                        table -> table,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));

        return standardTables.values().stream()
                .sorted(Comparator.comparing(table -> getStandardTableNumber(table.getName())))
                .map(t -> {
                    Integer tableNumber = getStandardTableNumber(t.getName());
                    int[] defaultPosition = DEFAULT_TABLE_POSITIONS[tableNumber - 1];
                    Integer posX = t.getPosX() != null && t.getPosX() > 0 ? t.getPosX() : defaultPosition[0];
                    Integer posY = t.getPosY() != null && t.getPosY() > 0 ? t.getPosY() : defaultPosition[1];
                    // Lấy tất cả orders chưa completed của bàn
                    List<Order> orders = orderRepository.findByTableId(t.getId());
                    java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
                    String status = Boolean.TRUE.equals(t.getIsAvailable()) ? "trống" : "đang đặt";
                    Long activeOrderId = null;

                    for (Order order : orders) {
                        if (order.getStatus() == null || !"COMPLETED".equalsIgnoreCase(order.getStatus())) {
                            activeOrderId = order.getId();
                            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
                            for (OrderItem item : orderItems) {
                                int qty = item.getEffectiveQuantity();
                                totalAmount = totalAmount.add(item.getMenuItem().getPrice().multiply(new java.math.BigDecimal(qty)));
                            }
                            // Có thể cập nhật status động hơn ở đây nếu muốn
                        }
                    }

                    return TableListItemResponse.builder()
                            .id(t.getId())
                            .name(t.getName())
                            .capacity(t.getCapacity())
                            .isAvailable(t.getIsAvailable())
                            .status(status)
                            .totalAmount(totalAmount)
                            .customers(t.getCapacity())
                            .activeOrderId(activeOrderId)
                            .areaId(t.getAreaId())
                            .posX(posX)
                            .posY(posY)
                            .tableStatus(t.getTableStatus() != null ? t.getTableStatus().name() : "EMPTY")
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private Integer getStandardTableNumber(String name) {
        if (name == null) return null;
        String normalized = name.trim().replaceAll("\\s+", " ");
        if (!normalized.matches("(?i)^Bàn \\d+$")) return null;
        int number = Integer.parseInt(normalized.replaceAll("\\D+", ""));
        return number >= 1 && number <= MAX_STANDARD_TABLES ? number : null;
    }

    private String getDefaultTableName(int tableNumber) {
        return "Bàn " + tableNumber;
    }

    public TableDetailResponse getTableDetail(UUID tableId) {
        TableEntity table = tableRepository.findById(tableId)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_FOUND));

        // Lấy tất cả orders của bàn
        List<Order> orders = orderRepository.findByTableId(tableId);
        
        // Lấy items từ tất cả orders của bàn (không chỉ activeOrder)
        Map<String, TableOrderItemSummary> groupedItems = new HashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // Lấy items từ tất cả orders của bàn (không chỉ activeOrder)
        for (Order order : orders) {
            // Chỉ lấy items từ orders chưa completed
            if (order.getStatus() == null || !"COMPLETED".equalsIgnoreCase(order.getStatus())) {
                List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
                
                for (OrderItem item : orderItems) {
                    // Tạo key duy nhất cho mỗi món dựa trên menuItemId và notes (không gộp theo status nữa)
                    String key = item.getMenuItem().getId() + "__" + (item.getNotes() != null ? item.getNotes() : "");
                    
                    if (groupedItems.containsKey(key)) {
                        // Nếu đã có món giống, cộng dồn các lượng theo trạng thái
                        TableOrderItemSummary existing = groupedItems.get(key);
                        existing.setPendingQuantity((existing.getPendingQuantity() == null ? 0 : existing.getPendingQuantity()) + (item.getPendingQuantity() == null ? 0 : item.getPendingQuantity()));
                        existing.setPreparingQuantity((existing.getPreparingQuantity() == null ? 0 : existing.getPreparingQuantity()) + (item.getPreparingQuantity() == null ? 0 : item.getPreparingQuantity()));
                        existing.setCompletedQuantity((existing.getCompletedQuantity() == null ? 0 : existing.getCompletedQuantity()) + (item.getCompletedQuantity() == null ? 0 : item.getCompletedQuantity()));
                        existing.setServedQuantity((existing.getServedQuantity() == null ? 0 : existing.getServedQuantity()) + (item.getServedQuantity() == null ? 0 : item.getServedQuantity()));
                        existing.setCancelledQuantity((existing.getCancelledQuantity() == null ? 0 : existing.getCancelledQuantity()) + (item.getCancelledQuantity() == null ? 0 : item.getCancelledQuantity()));
                        existing.setOutOfStockQuantity((existing.getOutOfStockQuantity() == null ? 0 : existing.getOutOfStockQuantity()) + (item.getOutOfStockQuantity() == null ? 0 : item.getOutOfStockQuantity()));
                        int eff = item.getEffectiveQuantity();
                        existing.setTotalQuantity((existing.getTotalQuantity() == null ? 0 : existing.getTotalQuantity()) + eff);
                        
                        // Re-derive status after merging quantities - ưu tiên trạng thái cao nhất
                        String mergedStatus = "PENDING";
                        if (existing.getCancelledQuantity() != null && existing.getCancelledQuantity() > 0) {
                            mergedStatus = "CANCELLED";
                        } else if (existing.getOutOfStockQuantity() != null && existing.getOutOfStockQuantity() > 0) {
                            mergedStatus = "OUT_OF_STOCK";
                        } else if (existing.getServedQuantity() != null && existing.getServedQuantity() > 0) {
                            mergedStatus = "SERVED";
                        } else if (existing.getCompletedQuantity() != null && existing.getCompletedQuantity() > 0) {
                            mergedStatus = "COMPLETED";
                        } else if (existing.getPreparingQuantity() != null && existing.getPreparingQuantity() > 0) {
                            mergedStatus = "PREPARING";
                        } else if (existing.getPendingQuantity() != null && existing.getPendingQuantity() > 0) {
                            mergedStatus = "PENDING";
                        }
                        existing.setStatus(mergedStatus);
                        
                        // Cập nhật thời gian order thành thời gian mới nhất
                        if (order.getCreatedAt().isAfter(existing.getOrderTime())) {
                            existing.setOrderTime(order.getCreatedAt());
                        }
                    } else {
                        // Nếu chưa có, thêm mới
                        // Derive status from quantities - ưu tiên trạng thái cao nhất
                        String itemStatus = "PENDING";
                        if (item.getCancelledQuantity() != null && item.getCancelledQuantity() > 0) {
                            itemStatus = "CANCELLED";
                        } else if (item.getOutOfStockQuantity() != null && item.getOutOfStockQuantity() > 0) {
                            itemStatus = "OUT_OF_STOCK";
                        } else if (item.getServedQuantity() != null && item.getServedQuantity() > 0) {
                            itemStatus = "SERVED";
                        } else if (item.getCompletedQuantity() != null && item.getCompletedQuantity() > 0) {
                            itemStatus = "COMPLETED";
                        } else if (item.getPreparingQuantity() != null && item.getPreparingQuantity() > 0) {
                            itemStatus = "PREPARING";
                        } else if (item.getPendingQuantity() != null && item.getPendingQuantity() > 0) {
                            itemStatus = "PENDING";
                        }

                        TableOrderItemSummary summary = TableOrderItemSummary.builder()
                                .id(item.getId())
                                .menuItemId(item.getMenuItem().getId())
                                .menuItemName(item.getMenuItem().getName())
                                .menuItemImageUrl(item.getMenuItem().getImageUrl())
                                .pendingQuantity(item.getPendingQuantity())
                                .preparingQuantity(item.getPreparingQuantity())
                                .completedQuantity(item.getCompletedQuantity())
                                .servedQuantity(item.getServedQuantity())
                                .cancelledQuantity(item.getCancelledQuantity())
                                .outOfStockQuantity(item.getOutOfStockQuantity())
                                .totalQuantity(item.getEffectiveQuantity())
                                .unitPrice(item.getMenuItem().getPrice())
                                .status(itemStatus) // Set derived status
                                .orderId(order.getId()) // ID của order chứa item này
                                .orderStatus(order.getStatus()) // Trạng thái của order chứa item này
                                .orderTime(order.getCreatedAt()) // Thời gian tạo order chứa item này
                                .notes(item.getNotes()) // Ghi chú cho món ăn
                                .build();
                        
                        groupedItems.put(key, summary);
                    }
                    
                    totalAmount = totalAmount.add(item.getMenuItem().getPrice().multiply(new BigDecimal(item.getEffectiveQuantity())));
                }
            }
        }
        
        // Chuyển đổi map thành list và sắp xếp theo thời gian order (mới nhất trước)
        List<TableOrderItemSummary> sessionItems = new ArrayList<>(groupedItems.values());
        sessionItems.sort((a, b) -> b.getOrderTime().compareTo(a.getOrderTime()));

        return TableDetailResponse.builder()
                .tableId(table.getId())
                .tableName(table.getName())
                .customers(table.getCapacity())
                .status(Boolean.TRUE.equals(table.getIsAvailable()) ? "trống" : "đang đặt")
                .totalAmount(totalAmount)
                .activeOrderId(orders.stream()
                        .filter(o -> o.getStatus() == null || !"COMPLETED".equalsIgnoreCase(o.getStatus()))
                        .findFirst()
                        .map(Order::getId)
                        .orElse(null))
                .sessionItems(sessionItems) // Items từ giỏ hàng đang hoạt động
                .build();
    }

    public void clearTable(UUID tableId) {
        TableEntity table = tableRepository.findById(tableId)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_FOUND));

        // Kiểm tra xem có món nào chưa được phục vụ không - CHỈ KIỂM TRA ORDERS ĐANG MỞ
        List<Order> orders = orderRepository.findByTableId(tableId);
        boolean hasUnservedItems = false;
        
        for (Order order : orders) {
            // CHỈ kiểm tra orders chưa completed (đang mở)
            if (order.getStatus() == null || !"COMPLETED".equalsIgnoreCase(order.getStatus())) {
                List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
                for (OrderItem item : orderItems) {
                    // Debug: Log item quantities to understand the issue
                    log.info("Item {} - Pending: {}, Preparing: {}, Completed: {}, Served: {}", 
                        item.getId(), 
                        item.getPendingQuantity(), 
                        item.getPreparingQuantity(), 
                        item.getCompletedQuantity(), 
                        item.getServedQuantity());
                    
                    // Unserved if any quantity remains in pending, preparing, or completed
                    // Only served items are considered fully served
                    if ((item.getPendingQuantity() != null && item.getPendingQuantity() > 0)
                            || (item.getPreparingQuantity() != null && item.getPreparingQuantity() > 0)
                            || (item.getCompletedQuantity() != null && item.getCompletedQuantity() > 0)) {
                        hasUnservedItems = true;
                        log.info("Found unserved item: {} with quantities - Pending: {}, Preparing: {}, Completed: {}", 
                            item.getId(), item.getPendingQuantity(), item.getPreparingQuantity(), item.getCompletedQuantity());
                        break;
                    }
                }
                if (hasUnservedItems) break;
            }
        }
        
        if (hasUnservedItems) {
            throw new AppException(ErrorCode.TABLE_CANNOT_CLEAR);
        }

        // Đánh dấu các order hiện tại của bàn là completed nếu đang mở
        for (Order order : orders) {
            if (!"COMPLETED".equalsIgnoreCase(order.getStatus())) {
                order.setStatus("COMPLETED");
                orderRepository.save(order);
            }
        }

        // Hủy giỏ hàng đang hoạt động của bàn (nếu có) - chỉ set isActive=false, giữ lại cart items để kiểm tra
        cartRepository.findAllByTableIdAndIsActiveTrue(tableId).forEach(cart -> {
            cart.setIsActive(false);
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
        });

        // Giải phóng bàn: đặt isAvailable = true
        table.setIsAvailable(true);
        tableRepository.save(table);

        // Phát sự kiện realtime để client (khách) xóa cache local
        eventPublisher.publishEvent(new TableClearedEvent(tableId));
        // Sau khi thay đổi trạng thái bàn (trong các phương thức liên quan)
        eventPublisher.publishEvent(new TableStatusChangedEvent());
    }

    // Sự kiện domain phát khi dọn bàn
    @lombok.Value
    public static class TableClearedEvent {
        UUID tableId;
    }

    public TableListItemResponse toListItem(TableEntity t) {
        List<Order> orders = orderRepository.findByTableId(t.getId());
        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
        String status = Boolean.TRUE.equals(t.getIsAvailable()) ? "trống" : "đang đặt";
        Long activeOrderId = null;
        for (Order order : orders) {
            if (order.getStatus() == null || !"COMPLETED".equalsIgnoreCase(order.getStatus())) {
                activeOrderId = order.getId();
                List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
                for (OrderItem item : orderItems) {
                    totalAmount = totalAmount.add(item.getMenuItem().getPrice().multiply(new java.math.BigDecimal(item.getEffectiveQuantity())));
                }
            }
        }
        return TableListItemResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .capacity(t.getCapacity())
                .isAvailable(t.getIsAvailable())
                .status(status)
                .totalAmount(totalAmount)
                .customers(t.getCapacity())
                .activeOrderId(activeOrderId)
                .areaId(t.getAreaId())
                .posX(t.getPosX())
                .posY(t.getPosY())
                .tableStatus(t.getTableStatus() != null ? t.getTableStatus().name() : "EMPTY")
                .build();
    }
}
