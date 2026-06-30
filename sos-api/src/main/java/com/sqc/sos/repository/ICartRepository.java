package com.sqc.sos.repository;

import com.sqc.sos.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ICartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findBySessionIdAndIsActiveTrue(String sessionId);

    Optional<Cart> findByTableIdAndIsActiveTrue(UUID tableId);

    List<Cart> findAllByTableIdAndIsActiveTrue(UUID tableId);

    boolean existsBySessionIdAndIsActiveTrue(String sessionId);
} 
