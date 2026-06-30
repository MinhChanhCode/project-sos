package com.sqc.sos.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", columnDefinition = "binary(16)")
    TableEntity table;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    List<CartItem> cartItems = new ArrayList<>();

    @Column(nullable = false)
    String sessionId; // Để track giỏ hàng theo session

    @Column(nullable = false)
    Boolean isActive;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @Column
    LocalDateTime updatedAt;

    // Helper methods
    public BigDecimal getTotalAmount() {
        if (cartItems == null) {
            return BigDecimal.ZERO;
        }
        return cartItems.stream()
                .filter(item -> item != null && item.getMenuItem() != null && Boolean.TRUE.equals(item.getIsActive()))
                .map(item -> item.getMenuItem().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Integer getTotalItems() {
        if (cartItems == null) {
            return 0;
        }
        return cartItems.stream()
                .filter(item -> item != null && Boolean.TRUE.equals(item.getIsActive()))
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public void addItem(CartItem item) {
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        cartItems.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item) {
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        cartItems.remove(item);
        item.setCart(null);
    }

    // Helper method to get only active cart items
    public List<CartItem> getActiveCartItems() {
        if (cartItems == null) {
            return new ArrayList<>();
        }
        return cartItems.stream()
                .filter(item -> item != null && Boolean.TRUE.equals(item.getIsActive()))
                .collect(Collectors.toList());
    }
} 