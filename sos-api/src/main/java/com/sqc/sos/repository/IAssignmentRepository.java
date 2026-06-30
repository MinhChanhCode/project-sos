package com.sqc.sos.repository;

import com.sqc.sos.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IAssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByEmployeeId(UUID employeeId);
    List<Assignment> findByAreaId(Long areaId);
}
