package com.sqc.sos.controller;

import com.sqc.sos.dto.ApiResponse;
import com.sqc.sos.dto.qr.QrCodeResponse;
import com.sqc.sos.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/qr-codes")
@RequiredArgsConstructor
public class QrCodeController {
    private final QrCodeService qrCodeService;

    @GetMapping("/table/{tableId}")
    public ResponseEntity<ApiResponse<List<QrCodeResponse>>> listByTable(@PathVariable UUID tableId) {
        return ResponseEntity.ok(ApiResponse.success(qrCodeService.listByTable(tableId), "Lấy QR thành công"));
    }

    @PostMapping("/table/{tableId}/generate")
    public ResponseEntity<ApiResponse<QrCodeResponse>> generate(@PathVariable UUID tableId) {
        return ResponseEntity.ok(ApiResponse.success(qrCodeService.generate(tableId), "Tạo QR thành công"));
    }

    @GetMapping("/token/{token}")
    public ResponseEntity<ApiResponse<QrCodeResponse>> resolve(@PathVariable String token) {
        return ResponseEntity.ok(ApiResponse.success(qrCodeService.resolveByToken(token), "Quét QR thành công"));
    }
}
