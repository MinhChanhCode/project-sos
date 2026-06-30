package com.sqc.sos.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    User recipient;

    String title;
    String message;

    @Enumerated(EnumType.STRING)
    NotificationType type;

    @Enumerated(EnumType.STRING)
    NotificationPriority priority;

    Boolean isRead;
    LocalDateTime readAt;

    @Column(columnDefinition = "json")
    String metadata;

    public enum NotificationType {
        KITCHEN, CALL, ORDER, PAYMENT, SYSTEM
    }

    public enum NotificationPriority {
        LOW, MEDIUM, HIGH, URGENT
    }
} 