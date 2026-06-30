package com.sqc.sos.repository;

import com.sqc.sos.model.TableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ITableRepository extends JpaRepository<TableEntity, UUID> {

    List<TableEntity> findByIsAvailableTrue();

    boolean existsByName(String name);

    Optional<TableEntity> findByName(String name);

    Optional<TableEntity> findFirstByNameOrderByIdAsc(String name);
} 
