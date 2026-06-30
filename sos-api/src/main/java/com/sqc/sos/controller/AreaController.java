package com.sqc.sos.controller;

import com.sqc.sos.dto.ApiResponse;
import com.sqc.sos.dto.area.AreaRequest;
import com.sqc.sos.dto.area.AreaResponse;
import com.sqc.sos.service.AreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/areas")
@RequiredArgsConstructor
public class AreaController {
    private final AreaService areaService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AreaResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(areaService.listAll(), "Lấy danh sách khu vực thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AreaResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(areaService.getById(id), "Lấy khu vực thành công"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AreaResponse>> create(@RequestBody AreaRequest request) {
        return ResponseEntity.ok(ApiResponse.success(areaService.create(request), "Tạo khu vực thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AreaResponse>> update(@PathVariable Long id, @RequestBody AreaRequest request) {
        return ResponseEntity.ok(ApiResponse.success(areaService.update(id, request), "Cập nhật khu vực thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        areaService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa khu vực thành công"));
    }
}
