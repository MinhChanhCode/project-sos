package com.sqc.sos.repository;

import com.sqc.sos.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ICategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByIsActiveTrue();
    
    boolean existsByName(String name);
} 