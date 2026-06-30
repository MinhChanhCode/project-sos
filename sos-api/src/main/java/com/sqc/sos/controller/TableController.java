package com.sqc.sos.controller;

import com.sqc.sos.dto.ApiResponse;
import com.sqc.sos.dto.table.TableDetailResponse;
import com.sqc.sos.dto.table.TableListItemResponse;
import com.sqc.sos.dto.table.TablePositionBatchRequest;
import com.sqc.sos.dto.table.TableRequest;
import com.sqc.sos.model.TableEntity;
import com.sqc.sos.service.TableManagementService;
import com.sqc.sos.service.TableQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableQueryService tableQueryService;
    private final TableManagementService tableManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<java.util.List<TableListItemResponse>>> listTables() {
        log.info("GET /tables");
        var tables = tableQueryService.listAllTables();
        return ResponseEntity.ok(ApiResponse.success(tables, "Lấy danh sách bàn thành công"));
    }

    @GetMapping("/{tableId}/detail")
    public ResponseEntity<ApiResponse<TableDetailResponse>> getTableDetail(@PathVariable UUID tableId) {
        log.info("GET /tables/{}/detail", tableId);
        TableDetailResponse detail = tableQueryService.getTableDetail(tableId);
        return ResponseEntity.ok(ApiResponse.success(detail, "Lấy chi tiết bàn thành công"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TableListItemResponse>> create(@RequestBody TableRequest request) {
        TableEntity table = tableManagementService.create(request);
        return ResponseEntity.ok(ApiResponse.success(tableQueryService.toListItem(table), "Tạo bàn thành công"));
    }

    @PutMapping("/{tableId}")
    public ResponseEntity<ApiResponse<TableListItemResponse>> update(@PathVariable UUID tableId, @RequestBody TableRequest request) {
        TableEntity table = tableManagementService.update(tableId, request);
        return ResponseEntity.ok(ApiResponse.success(tableQueryService.toListItem(table), "Cập nhật bàn thành công"));
    }

    @PatchMapping("/positions")
    public ResponseEntity<ApiResponse<Void>> updatePositions(@RequestBody TablePositionBatchRequest request) {
        tableManagementService.updatePositions(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Cập nhật vị trí bàn thành công"));
    }

    @PatchMapping("/{tableId}/status")
    public ResponseEntity<ApiResponse<TableListItemResponse>> updateStatus(@PathVariable UUID tableId, @RequestParam String status) {
        TableEntity table = tableManagementService.updateStatus(tableId, status);
        return ResponseEntity.ok(ApiResponse.success(tableQueryService.toListItem(table), "Cập nhật trạng thái bàn thành công"));
    }

    @PatchMapping("/{tableId}/clear")
    public ResponseEntity<ApiResponse<Void>> clearTable(@PathVariable UUID tableId) {
        log.info("PATCH /tables/{}/clear", tableId);
        tableQueryService.clearTable(tableId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã dọn bàn và giải phóng cho khách mới"));
    }

    @DeleteMapping("/{tableId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID tableId) {
        tableManagementService.delete(tableId);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa bàn thành công"));
    }
}
