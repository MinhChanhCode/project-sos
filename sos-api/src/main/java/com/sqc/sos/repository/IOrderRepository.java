package com.sqc.sos.repository;

import com.sqc.sos.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IOrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByTableId(UUID tableId);
    List<Order> findByStatus(String status);
    
    // Tìm Order theo tableId và status để đảm bảo mỗi bàn chỉ có một Order PENDING
    Optional<Order> findByTableIdAndStatus(UUID tableId, String status);
} 