package com.sqc.sos.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "menu_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;
    String description;
    BigDecimal price;
    String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", columnDefinition = "char(36)")
    Category category;

    Boolean isAvailable;
    Boolean isActive;

    @Column(length = 80)
    String type;

    @Column(length = 500)
    String tasteTags;

    Integer spicyLevel;

    @Column(length = 800)
    String ingredients;

    @Column(length = 500)
    String allergens;

    @Column(length = 500)
    String suitableFor;

    @Column(length = 500)
    String pairing;

    Boolean isVegetarian;

    Integer prepTimeMinutes;

    // Promotion fields (nullable)
    BigDecimal originalPrice;
    BigDecimal promotionalPrice;
    LocalDateTime promotionEndDate;
} 
