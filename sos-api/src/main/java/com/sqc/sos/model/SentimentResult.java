package com.sqc.sos.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sentiment_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SentimentResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    Long reviewId;
    String sentiment;
    BigDecimal confidence;
    LocalDateTime analyzedAt;

    @PrePersist
    void prePersist() {
        if (analyzedAt == null) analyzedAt = LocalDateTime.now();
    }
}
