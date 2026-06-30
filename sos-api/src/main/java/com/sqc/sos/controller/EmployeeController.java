package com.sqc.sos.controller;

import com.sqc.sos.dto.ApiResponse;
import com.sqc.sos.dto.employee.*;
import com.sqc.sos.model.Assignment;
import com.sqc.sos.service.EmployeeShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeShiftService employeeShiftService;

    @GetMapping("/api/v1/employees")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> listEmployees() {
        return ResponseEntity.ok(ApiResponse.success(employeeShiftService.listEmployees(), "Lấy nhân viên thành công"));
    }

    @PostMapping("/api/v1/employees")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(@RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeShiftService.createEmployee(request), "Tạo nhân viên thành công"));
    }

    @PutMapping("/api/v1/employees/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(@PathVariable UUID id, @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeShiftService.updateEmployee(id, request), "Cập nhật nhân viên thành công"));
    }

    @DeleteMapping("/api/v1/employees/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable UUID id) {
        employeeShiftService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa nhân viên thành công"));
    }

    @GetMapping("/api/v1/shifts")
    public ResponseEntity<ApiResponse<List<ShiftResponse>>> listShifts() {
        return ResponseEntity.ok(ApiResponse.success(employeeShiftService.listShifts(), "Lấy ca làm thành công"));
    }

    @PostMapping("/api/v1/shifts")
    public ResponseEntity<ApiResponse<ShiftResponse>> createShift(@RequestBody ShiftRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeShiftService.createShift(request), "Tạo ca làm thành công"));
    }

    @PostMapping("/api/v1/assignments")
    public ResponseEntity<ApiResponse<Assignment>> assign(@RequestParam UUID employeeId, @RequestParam Long areaId, @RequestParam(required = false) Long shiftId) {
        return ResponseEntity.ok(ApiResponse.success(employeeShiftService.assignArea(employeeId, areaId, shiftId), "Phân công thành công"));
    }

    @GetMapping("/api/v1/assignments")
    public ResponseEntity<ApiResponse<List<Assignment>>> listAssignments() {
        return ResponseEntity.ok(ApiResponse.success(employeeShiftService.listAssignments(), "Lấy phân công thành công"));
    }
}
