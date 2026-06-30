package com.sqc.sos.repository;

import com.sqc.sos.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ICartItemRepository extends JpaRepository<CartItem, UUID> {

    List<CartItem> findByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndMenuItemId(Long cartId, Long menuItemId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.menuItem.id = :menuItemId")
    Optional<CartItem> findByCartIdAndMenuItemIdWithDetails(@Param("cartId") UUID cartId, @Param("menuItemId") UUID menuItemId);

    void deleteByCartId(Long cartId);

    Page<CartItem> findByCartId(Long cartId, Pageable pageable);

    List<CartItem> findByCartIdAndIsActiveTrue(Long cartId);
    Optional<CartItem> findByCartIdAndMenuItemIdAndIsActiveTrue(Long cartId, Long menuItemId);
    Page<CartItem> findByCartIdAndIsActiveTrue(Long cartId, Pageable pageable);
} 