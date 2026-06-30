package com.sqc.sos.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", columnDefinition = "char(36)")
    Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", columnDefinition = "char(36)")
    MenuItem menuItem;

    @Column(name = "pending_quantity", nullable = false)
    @Builder.Default
    Integer pendingQuantity = 0;

    @Column(name = "preparing_quantity", nullable = false)
    @Builder.Default
    Integer preparingQuantity = 0;

    @Column(name = "completed_quantity", nullable = false)
    @Builder.Default
    Integer completedQuantity = 0; // Số lượng đã hoàn thành

    @Column(name = "served_quantity", nullable = false)
    @Builder.Default
    Integer servedQuantity = 0;

    @Column(name = "cancelled_quantity", nullable = false)
    @Builder.Default
    Integer cancelledQuantity = 0;

    @Column(name = "out_of_stock_quantity", nullable = false)
    @Builder.Default
    Integer outOfStockQuantity = 0;

    String notes; // Ghi chú cho món ăn (ví dụ: không cay, thêm rau...)

    public int getEffectiveQuantity() {
        // Số lượng có tính tiền: bỏ qua cancelled và out_of_stock
        return safe(pendingQuantity) + safe(preparingQuantity) + safe(completedQuantity) + safe(servedQuantity);
    }

    public int getQuantityByStatus(OrderItemStatus statusEnum) {
        switch (statusEnum) {
            case PENDING: return safe(pendingQuantity);
            case PREPARING: return safe(preparingQuantity);
            case COMPLETED: return safe(completedQuantity);
            case SERVED: return safe(servedQuantity);
            case CANCELLED: return safe(cancelledQuantity);
            case OUT_OF_STOCK: return safe(outOfStockQuantity);
            default: return 0;
        }
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }
} 