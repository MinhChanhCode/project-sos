package com.sqc.sos.controller;

import com.sqc.sos.dto.ApiResponse;
import com.sqc.sos.dto.user.UserDetailResponse;
import com.sqc.sos.dto.user.UserProfileUpdateRequest;
import com.sqc.sos.dto.user.UserRequest;
import com.sqc.sos.dto.user.UserResponse;
import com.sqc.sos.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserManagementService userManagementService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDetailResponse>> me() {
        return ResponseEntity.ok(ApiResponse.success(userManagementService.getCurrentUser(), "Lấy thông tin user thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDetailResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(userManagementService.listAll(), "Lấy danh sách user thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userManagementService.getById(id), "Lấy user thành công"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@RequestBody UserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userManagementService.create(request), "Tạo user thành công"));
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<UserDetailResponse>> updateProfile(@PathVariable UUID id, @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userManagementService.updateProfile(id, request), "Cập nhật profile thành công"));
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<UserDetailResponse>> assignRoles(@PathVariable UUID id, @RequestBody List<String> roles) {
        return ResponseEntity.ok(ApiResponse.success(userManagementService.assignRoles(id, roles), "Gán role thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        userManagementService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa user thành công"));
    }
}
