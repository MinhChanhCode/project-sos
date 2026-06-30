package com.sqc.sos.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqc.sos.model.Notification;
import com.sqc.sos.model.OrderItem;
import com.sqc.sos.repository.INotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final INotificationRepository notificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

        @EventListener
        public void onOrderItemStatusChanged(OrderItemService.OrderItemStatusChangedEvent event) {
            OrderItem item = event.getOrderItem();

            try {
                String status = deriveStatus(item);
                String metadata = objectMapper.writeValueAsString(new Metadata(item.getId(), status));

                Notification notification = Notification.builder()
                        .recipient(null) // có thể set staff/customer cụ thể sau
                        .title("Trạng thái món đã thay đổi")
                        .message("Món #" + item.getId() + " cập nhật: " + status)
                        .type(Notification.NotificationType.ORDER)
                        .priority(Notification.NotificationPriority.MEDIUM)
                        .isRead(false)
                        .readAt(null)
                        .metadata(metadata)
                        .build();
                notificationRepository.save(notification);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize notification metadata", e);
            }
        }

    private String deriveStatus(OrderItem item) {
        if (item.getServedQuantity() != null && item.getServedQuantity() > 0
                && (safe(item.getPendingQuantity()) + safe(item.getPreparingQuantity()) + safe(item.getCompletedQuantity())) == 0) {
            return "SERVED";
        }
        if (item.getCompletedQuantity() != null && item.getCompletedQuantity() > 0
                && (safe(item.getPendingQuantity()) + safe(item.getPreparingQuantity())) == 0) {
            return "COMPLETED";
        }
        if (item.getPreparingQuantity() != null && item.getPreparingQuantity() > 0) {
            return "PREPARING";
        }
        if (item.getPendingQuantity() != null && item.getPendingQuantity() > 0) {
            return "PENDING";
        }
        if (item.getCancelledQuantity() != null && item.getCancelledQuantity() > 0) {
            return "CANCELLED";
        }
        if (item.getOutOfStockQuantity() != null && item.getOutOfStockQuantity() > 0) {
            return "OUT_OF_STOCK";
        }
        return "UNKNOWN";
    }

    private int safe(Integer v) { return v == null ? 0 : v; }

    private record Metadata(Long orderItemId, String status) {}
}


