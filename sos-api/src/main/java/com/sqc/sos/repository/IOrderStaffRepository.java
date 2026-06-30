package com.sqc.sos.repository;

import com.sqc.sos.model.OrderStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IOrderStaffRepository extends JpaRepository<OrderStaff, Integer> {
    List<OrderStaff> findByOrderId(Integer orderId);
    List<OrderStaff> findByStaffId(UUID staffId);
} 