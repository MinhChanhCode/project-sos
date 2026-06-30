package com.sqc.sos.repository;

import com.sqc.sos.model.CustomerSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ICustomerSessionRepository extends JpaRepository<CustomerSession, Long> {
    Optional<CustomerSession> findBySessionId(String sessionId);
    Optional<CustomerSession> findFirstByTableIdAndIsActiveTrueOrderByUpdatedAtDesc(UUID tableId);
}
