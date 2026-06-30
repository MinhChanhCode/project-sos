package com.sqc.sos.repository;

import com.sqc.sos.model.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findTop20BySessionIdOrderByCreatedAtDesc(String sessionId);
}
