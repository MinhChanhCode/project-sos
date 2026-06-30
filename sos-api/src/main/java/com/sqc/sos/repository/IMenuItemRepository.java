
package com.sqc.sos.repository;

import com.sqc.sos.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

@Repository
public interface IMenuItemRepository extends JpaRepository<MenuItem, UUID> {

    List<MenuItem> findByIsActiveTrue();

    List<MenuItem> findByCategoryIdAndIsActiveTrue(Long categoryId);

    @Query("SELECT m FROM MenuItem m WHERE m.isActive = true AND (m.name LIKE %:keyword% OR m.description LIKE %:keyword%)")
    List<MenuItem> searchByKeyword(@Param("keyword") String keyword);

    List<MenuItem> findByIsAvailableTrueAndIsActiveTrue();

    Optional<MenuItem> findById(Long id);

    Page<MenuItem> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<MenuItem> findByIsActiveTrue(Pageable pageable);
    Page<MenuItem> findByIsAvailableTrueAndIsActiveTrue(Pageable pageable);
    Page<MenuItem> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);
    @Query("SELECT m FROM MenuItem m WHERE m.isActive = true AND (m.name LIKE %:keyword% OR m.description LIKE %:keyword%)")
    Page<MenuItem> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT m FROM MenuItem m WHERE m.isActive = true AND m.promotionalPrice IS NOT NULL AND m.promotionalPrice < m.price AND (m.promotionEndDate IS NULL OR m.promotionEndDate > :now) ORDER BY m.promotionEndDate NULLS LAST, m.id ASC")
    List<MenuItem> findActivePromotions(@Param("now") LocalDateTime now);
}