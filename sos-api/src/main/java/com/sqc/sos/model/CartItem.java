package com.sqc.sos.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", columnDefinition = "binary(16)")
    Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", columnDefinition = "binary(16)")
    MenuItem menuItem;

    @Column(nullable = false)
    Integer quantity;

    @Column(nullable = false)
    BigDecimal unitPrice; // Lưu giá tại thời điểm thêm vào giỏ

    @Column(nullable = false)
    @Builder.Default
    Boolean isActive = true;

    @Column
    String notes; // Ghi chú cho món ăn (ví dụ: không cay, thêm rau...)

    // Helper methods
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
} 