package com.sqc.sos.repository;

import com.sqc.sos.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IEmployeeRepository extends JpaRepository<Employee, UUID> {
}
