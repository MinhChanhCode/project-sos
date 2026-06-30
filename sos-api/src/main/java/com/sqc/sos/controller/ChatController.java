package com.sqc.sos.controller;

import com.sqc.sos.dto.ApiResponse;
import com.sqc.sos.dto.chat.ChatRequest;
import com.sqc.sos.dto.chat.ChatResponse;
import com.sqc.sos.model.ChatHistory;
import com.sqc.sos.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@RequestBody ChatRequest request) {
        return ResponseEntity.ok(ApiResponse.success(chatService.chat(request), "Chat thành công"));
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<ApiResponse<List<ChatHistory>>> history(@PathVariable String sessionId) {
        return ResponseEntity.ok(ApiResponse.success(chatService.getHistory(sessionId), "Lấy lịch sử chat thành công"));
    }
}
