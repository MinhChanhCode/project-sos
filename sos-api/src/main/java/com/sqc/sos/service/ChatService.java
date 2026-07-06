package com.sqc.sos.service;

import com.sqc.sos.dto.chat.ChatRequest;
import com.sqc.sos.dto.chat.ChatResponse;
import com.sqc.sos.model.ChatHistory;
import com.sqc.sos.model.MenuItem;
import com.sqc.sos.repository.IChatHistoryRepository;
import com.sqc.sos.repository.IMenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final IChatHistoryRepository chatHistoryRepository;
    private final IMenuItemRepository menuItemRepository;

    @Value("${ai.service.url:}")
    private String aiServiceUrl;

    @Transactional
    public ChatResponse chat(ChatRequest request) {
        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();
        String reply = callExternalAi(request.getMessage(), sessionId);
        if (reply == null || reply.isBlank()) {
            reply = buildLocalRagReply(request.getMessage());
        }
        ChatHistory history = ChatHistory.builder()
                .sessionId(sessionId)
                .userMessage(request.getMessage())
                .botResponse(reply)
                .build();
        history = chatHistoryRepository.save(history);
        return ChatResponse.builder()
                .sessionId(sessionId)
                .reply(reply)
                .historyId(history.getId())
                .build();
    }

    public List<ChatHistory> getHistory(String sessionId) {
        return chatHistoryRepository.findTop20BySessionIdOrderByCreatedAtDesc(sessionId);
    }

    private String callExternalAi(String message, String sessionId) {
        if (aiServiceUrl == null || aiServiceUrl.isBlank()) return null;
        try {
            WebClient client = WebClient.create(aiServiceUrl);
            Map<String, Object> body = Map.of("session_id", sessionId, "message", message);
            Map<?, ?> response = client.post()
                    .uri("/chat")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(Duration.ofSeconds(12));
            if (response != null && response.get("reply") != null) {
                return response.get("reply").toString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String buildLocalRagReply(String message) {
        String lower = message.toLowerCase(Locale.ROOT);
        List<MenuItem> items = menuItemRepository.findAll().stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsActive()) && Boolean.TRUE.equals(m.getIsAvailable()))
                .collect(Collectors.toList());

        BigDecimal maxBudget = extractBudget(lower);
        boolean noSpicy = lower.contains("không cay") || lower.contains("khong cay") || lower.contains("not spicy");

        List<MenuItem> matched = items.stream()
                .filter(m -> {
                    if (maxBudget != null && m.getPrice().compareTo(maxBudget) > 0) return false;
                    if (noSpicy && m.getDescription() != null && m.getDescription().toLowerCase(Locale.ROOT).contains("cay")) return false;
                    if (lower.contains("phở") || lower.contains("pho")) return m.getName().toLowerCase(Locale.ROOT).contains("phở");
                    if (lower.contains("combo")) return m.getName().toLowerCase(Locale.ROOT).contains("combo");
                    return lower.length() < 5 || m.getName().toLowerCase(Locale.ROOT).contains(lower)
                            || (m.getDescription() != null && m.getDescription().toLowerCase(Locale.ROOT).contains(lower));
                })
                .limit(5)
                .toList();

        if (matched.isEmpty()) {
            matched = items.stream().limit(3).toList();
        }

        if (matched.isEmpty()) {
            return "Xin chào! Hiện chưa có món trong menu. Vui lòng hỏi nhân viên để được tư vấn.";
        }

        StringBuilder sb = new StringBuilder("Dựa trên menu nhà hàng, tôi gợi ý:\n");
        for (MenuItem m : matched) {
            sb.append("- ").append(m.getName())
                    .append(" (").append(m.getPrice()).append("đ)");
            if (m.getDescription() != null && !m.getDescription().isBlank()) {
                sb.append(": ").append(m.getDescription());
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    private BigDecimal extractBudget(String text) {
        if (text.contains("100.000") || text.contains("100000")) return new BigDecimal("100000");
        if (text.contains("200.000") || text.contains("200000")) return new BigDecimal("200000");
        if (text.contains("50.000") || text.contains("50000")) return new BigDecimal("50000");
        return null;
    }
}
