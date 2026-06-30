package com.sqc.sos.service;

import com.sqc.sos.dto.employee.*;
import com.sqc.sos.exception.AppException;
import com.sqc.sos.exception.ErrorCode;
import com.sqc.sos.model.Assignment;
import com.sqc.sos.model.Employee;
import com.sqc.sos.model.StaffShift;
import com.sqc.sos.model.User;
import com.sqc.sos.repository.IAssignmentRepository;
import com.sqc.sos.repository.IEmployeeRepository;
import com.sqc.sos.repository.IStaffShiftRepository;
import com.sqc.sos.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeShiftService {
    private final IEmployeeRepository employeeRepository;
    private final IStaffShiftRepository staffShiftRepository;
    private final IAssignmentRepository assignmentRepository;
    private final IUserRepository userRepository;

    public List<EmployeeResponse> listEmployees() {
        return employeeRepository.findAll().stream().map(this::toEmployeeResponse).toList();
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        Employee employee = Employee.builder()
                .userId(request.getUserId())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .position(request.getPosition())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return toEmployeeResponse(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeResponse updateEmployee(UUID id, EmployeeRequest request) {
        Employee employee = findEmployee(id);
        if (request.getFullName() != null) employee.setFullName(request.getFullName());
        if (request.getPhone() != null) employee.setPhone(request.getPhone());
        if (request.getEmail() != null) employee.setEmail(request.getEmail());
        if (request.getPosition() != null) employee.setPosition(request.getPosition());
        if (request.getIsActive() != null) employee.setIsActive(request.getIsActive());
        return toEmployeeResponse(employeeRepository.save(employee));
    }

    @Transactional
    public void deleteEmployee(UUID id) {
        employeeRepository.delete(findEmployee(id));
    }

    public List<ShiftResponse> listShifts() {
        return staffShiftRepository.findAll().stream().map(this::toShiftResponse).toList();
    }

    @Transactional
    public ShiftResponse createShift(ShiftRequest request) {
        User staff = userRepository.findById(request.getStaffId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        StaffShift shift = StaffShift.builder()
                .staff(staff)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .role(request.getRole())
                .isCompleted(false)
                .build();
        return toShiftResponse(staffShiftRepository.save(shift));
    }

    @Transactional
    public Assignment assignArea(UUID employeeId, Long areaId, Long shiftId) {
        findEmployee(employeeId);
        return assignmentRepository.save(Assignment.builder()
                .employeeId(employeeId)
                .areaId(areaId)
                .shiftId(shiftId)
                .build());
    }

    public List<Assignment> listAssignments() {
        return assignmentRepository.findAll();
    }

    private Employee findEmployee(UUID id) {
        return employeeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    private EmployeeResponse toEmployeeResponse(Employee e) {
        return EmployeeResponse.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .fullName(e.getFullName())
                .phone(e.getPhone())
                .email(e.getEmail())
                .position(e.getPosition())
                .isActive(e.getIsActive())
                .build();
    }

    private ShiftResponse toShiftResponse(StaffShift shift) {
        return ShiftResponse.builder()
                .id(shift.getId())
                .staffId(shift.getStaff() != null ? shift.getStaff().getId() : null)
                .staffName(shift.getStaff() != null ? shift.getStaff().getUsername() : null)
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .role(shift.getRole())
                .isCompleted(shift.getIsCompleted())
                .build();
    }
}
