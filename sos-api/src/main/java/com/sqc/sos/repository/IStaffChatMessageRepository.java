package com.sqc.sos.repository;

import com.sqc.sos.model.StaffChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IStaffChatMessageRepository extends JpaRepository<StaffChatMessage, Long> {
    List<StaffChatMessage> findTop50ByTableIdOrderByCreatedAtAsc(UUID tableId);
}
