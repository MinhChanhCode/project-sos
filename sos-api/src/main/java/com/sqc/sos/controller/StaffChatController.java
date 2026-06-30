package com.sqc.sos.controller;

import com.sqc.sos.dto.ApiResponse;
import com.sqc.sos.dto.chat.StaffChatRequest;
import com.sqc.sos.dto.chat.StaffChatResponse;
import com.sqc.sos.service.StaffChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/staff-chat")
@RequiredArgsConstructor
public class StaffChatController {
    private final StaffChatService staffChatService;

    @PostMapping
    public ResponseEntity<ApiResponse<StaffChatResponse>> send(@RequestBody StaffChatRequest request) {
        return ResponseEntity.ok(ApiResponse.success(staffChatService.send(request), "Gửi tin nhắn thành công"));
    }

    @GetMapping("/table/{tableId}")
    public ResponseEntity<ApiResponse<List<StaffChatResponse>>> history(@PathVariable UUID tableId) {
        return ResponseEntity.ok(ApiResponse.success(staffChatService.history(tableId), "Lấy tin nhắn thành công"));
    }
}
