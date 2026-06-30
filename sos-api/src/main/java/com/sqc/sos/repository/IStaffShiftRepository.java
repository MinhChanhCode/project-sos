package com.sqc.sos.repository;

import com.sqc.sos.model.StaffShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IStaffShiftRepository extends JpaRepository<StaffShift, Integer> {
    List<StaffShift> findByStaffId(UUID staffId);
    List<StaffShift> findByIsCompleted(Boolean isCompleted);
} 