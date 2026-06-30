package com.sqc.sos.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "table_id", columnDefinition = "char(36)")
    String tableId;

    @Column(name = "table_name")
    String tableName;

    @Column(name = "session_id")
    String sessionId;

    @Column(name = "request_type")
    @Enumerated(EnumType.STRING)
    RequestType requestType;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    RequestStatus status;

    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    RequestPriority priority;

    @Column(name = "requested_at")
    LocalDateTime requestedAt;

    @Column(name = "started_at")
    LocalDateTime startedAt;

    @Column(name = "completed_at")
    LocalDateTime completedAt;

    @Column(name = "assigned_to", columnDefinition = "char(36)")
    String assignedTo;

    @Column(name = "notes", columnDefinition = "TEXT")
    String notes;

    public enum RequestType {
        GENERAL_SERVICE, // Dịch vụ chung
        FOOD_REQUEST, // Yêu cầu về món ăn
        CLEANING, // Dọn dẹp
        PAYMENT, // Thanh toán
        OTHER // Khác
    }

    public enum RequestStatus {
        PENDING, // Chờ xử lý
        IN_PROGRESS, // Đang xử lý
        DONE, // Hoàn thành
        CANCELLED // Đã hủy
    }

    public enum RequestPriority {
        LOW, // Thấp
        MEDIUM, // Trung bình
        HIGH, // Cao
        URGENT // Khẩn cấp
    }
}










