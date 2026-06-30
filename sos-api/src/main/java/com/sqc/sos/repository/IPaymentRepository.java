package com.sqc.sos.repository;

import com.sqc.sos.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IPaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByOrderId(Integer orderId);
    List<Payment> findByStatus(String status);
} 