package com.sqc.sos.model;

public enum OrderItemStatus {
    PENDING,        // Chờ xử lý
    PREPARING,      // Đang chế biến
    COMPLETED,      // Hoàn tất
    SERVED,         // Đã phục vụ
    CANCELLED,      // Đã hủy
    OUT_OF_STOCK    // Hết món
} 