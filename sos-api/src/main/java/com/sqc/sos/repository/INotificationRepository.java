package com.sqc.sos.repository;

import com.sqc.sos.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface INotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByRecipientId(UUID recipientId);
    List<Notification> findByIsRead(Boolean isRead);
    List<Notification> findByRecipientIdAndIsRead(UUID recipientId, Boolean isRead);
} 