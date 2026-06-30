package com.sqc.sos.repository;

import com.sqc.sos.model.OrderItem;
import com.sqc.sos.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface IOrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);
    
    @Query("SELECT oi FROM OrderItem oi WHERE (oi.pendingQuantity > 0 OR oi.preparingQuantity > 0) ORDER BY oi.order.createdAt DESC")
    List<OrderItem> findManagementCandidates();

    @Query("SELECT m FROM OrderItem oi JOIN oi.order o JOIN oi.menuItem m " +
            "WHERE (:from IS NULL OR o.createdAt >= :from) AND m.isActive = true AND m.isAvailable = true " +
            "GROUP BY m " +
            "ORDER BY (SUM(oi.pendingQuantity) + SUM(oi.preparingQuantity) + SUM(oi.completedQuantity) + SUM(oi.servedQuantity)) DESC")
    List<MenuItem> findBestSellerMenuItems(@Param("from") java.time.LocalDateTime from, Pageable pageable);
} 