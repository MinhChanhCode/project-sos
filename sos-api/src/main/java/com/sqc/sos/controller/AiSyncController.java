package com.sqc.sos.controller;

import com.sqc.sos.dto.ApiResponse;
import com.sqc.sos.service.MenuAiSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiSyncController {
    private final MenuAiSyncService menuAiSyncService;

    @PostMapping("/menu/sync")
    public ResponseEntity<ApiResponse<Map<String, Object>>> syncMenu() {
        return ResponseEntity.ok(ApiResponse.success(menuAiSyncService.syncMenu(), "Đồng bộ menu sang AI hoàn tất"));
    }
}
