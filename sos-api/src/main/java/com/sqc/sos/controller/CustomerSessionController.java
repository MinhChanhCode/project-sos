package com.sqc.sos.controller;

import com.sqc.sos.dto.ApiResponse;
import com.sqc.sos.dto.customer.CustomerSessionRequest;
import com.sqc.sos.dto.customer.CustomerSessionResponse;
import com.sqc.sos.service.CustomerSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer-sessions")
@RequiredArgsConstructor
public class CustomerSessionController {
    private final CustomerSessionService customerSessionService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerSessionResponse>> upsert(@RequestBody CustomerSessionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(customerSessionService.upsert(request), "Lưu phiên khách thành công"));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<CustomerSessionResponse>> get(@PathVariable String sessionId) {
        return ResponseEntity.ok(ApiResponse.success(customerSessionService.get(sessionId), "Lấy phiên khách thành công"));
    }
}
