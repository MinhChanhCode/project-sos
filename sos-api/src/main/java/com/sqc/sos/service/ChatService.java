package com.sqc.sos.service;

import com.sqc.sos.dto.chat.ChatRequest;
import com.sqc.sos.dto.chat.ChatResponse;
import com.sqc.sos.dto.table.TableDetailResponse;
import com.sqc.sos.model.Cart;
import com.sqc.sos.model.CartItem;
import com.sqc.sos.model.ChatHistory;
import com.sqc.sos.model.MenuItem;
import com.sqc.sos.model.Order;
import com.sqc.sos.repository.IChatHistoryRepository;
import com.sqc.sos.repository.ICartRepository;
import com.sqc.sos.repository.IMenuItemRepository;
import com.sqc.sos.repository.IOrderRepository;
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
    private final ICartRepository cartRepository;
    private final IOrderRepository orderRepository;
    private final TableQueryService tableQueryService;

    @Value("${ai.service.url:}")
    private String aiServiceUrl;

    @Transactional
    public ChatResponse chat(ChatRequest request) {
        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();
        ChatAiResult aiResult = callExternalAi(request, sessionId);
        String reply = aiResult.reply();
        String intent = aiResult.intent();
        List<Map<String, Object>> suggestedItems = aiResult.suggestedItems();
        List<Map<String, Object>> actions = aiResult.actions();
        List<String> usedTools = aiResult.usedTools();
        boolean memoryUpdated = aiResult.memoryUpdated();

        if (reply == null || reply.isBlank()) {
            reply = buildLocalRagReply(request.getMessage());
            intent = intent != null ? intent : detectLocalIntent(request.getMessage());
            usedTools = List.of("local_menu_rag");
            memoryUpdated = false;
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
                .intent(intent)
                .suggestedItems(suggestedItems)
                .actions(actions)
                .usedTools(usedTools)
                .memoryUpdated(memoryUpdated)
                .historyId(history.getId())
                .build();
    }

    public List<ChatHistory> getHistory(String sessionId) {
        return chatHistoryRepository.findTop20BySessionIdOrderByCreatedAtDesc(sessionId);
    }

    private ChatAiResult callExternalAi(ChatRequest request, String sessionId) {
        if (aiServiceUrl == null || aiServiceUrl.isBlank()) return ChatAiResult.empty();
        try {
            WebClient client = WebClient.create(aiServiceUrl);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("session_id", sessionId);
            body.put("message", request.getMessage());
            body.put("table_id", request.getTableId());
            body.put("table_number", request.getTableNumber());
            body.put("customer_name", request.getCustomerName());
            body.put("order_id", request.getOrderId());
            body.put("context", buildAiContext(request, sessionId));
            Map<?, ?> response = client.post()
                    .uri("/chat")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(Duration.ofSeconds(12));
            if (response != null) {
                return ChatAiResult.from(response);
            }
        } catch (Exception ignored) {
        }
        return ChatAiResult.empty();
    }

    private Map<String, Object> buildAiContext(ChatRequest request, String sessionId) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("customerName", request.getCustomerName());
        context.put("sessionId", sessionId);
        context.put("tableId", request.getTableId());
        context.put("tableNumber", request.getTableNumber());
        context.put("menu", menuItemRepository.findAll().stream().map(this::toMenuContext).toList());
        context.put("history", chatHistoryRepository.findTop20BySessionIdOrderByCreatedAtDesc(sessionId).stream()
                .sorted(Comparator.comparing(ChatHistory::getCreatedAt))
                .map(h -> Map.of(
                        "user", h.getUserMessage(),
                        "assistant", h.getBotResponse(),
                        "createdAt", h.getCreatedAt() != null ? h.getCreatedAt().toString() : ""
                ))
                .toList());

        UUID tableUuid = parseUuid(request.getTableId());
        if (tableUuid != null) {
            try {
                TableDetailResponse tableDetail = tableQueryService.getTableDetail(tableUuid);
                context.put("table", tableDetail);
            } catch (Exception ignored) {
            }
            context.put("orders", orderRepository.findByTableId(tableUuid).stream()
                    .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                    .limit(5)
                    .map(order -> Map.of(
                            "id", order.getId(),
                            "status", order.getStatus() != null ? order.getStatus() : "",
                            "createdAt", order.getCreatedAt() != null ? order.getCreatedAt().toString() : "",
                            "completedAt", order.getCompletedAt() != null ? order.getCompletedAt().toString() : ""
                    ))
                    .toList());
        }

        cartRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .ifPresent(cart -> context.put("cart", toCartContext(cart)));
        return context;
    }

    private Map<String, Object> toMenuContext(MenuItem item) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", item.getId());
        data.put("name", item.getName());
        data.put("description", item.getDescription());
        data.put("price", item.getPrice());
        data.put("imageUrl", item.getImageUrl());
        data.put("categoryId", item.getCategory() != null ? item.getCategory().getId() : null);
        data.put("categoryName", item.getCategory() != null ? item.getCategory().getName() : null);
        data.put("isAvailable", item.getIsAvailable());
        data.put("isActive", item.getIsActive());
        data.put("type", item.getType());
        data.put("tasteTags", item.getTasteTags());
        data.put("spicyLevel", item.getSpicyLevel());
        data.put("ingredients", item.getIngredients());
        data.put("allergens", item.getAllergens());
        data.put("suitableFor", item.getSuitableFor());
        data.put("pairing", item.getPairing());
        data.put("isVegetarian", item.getIsVegetarian());
        data.put("prepTimeMinutes", item.getPrepTimeMinutes());
        data.put("originalPrice", item.getOriginalPrice());
        data.put("promotionalPrice", item.getPromotionalPrice());
        data.put("promotionEndDate", item.getPromotionEndDate() != null ? item.getPromotionEndDate().toString() : null);
        return data;
    }

    private Map<String, Object> toCartContext(Cart cart) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", cart.getId());
        data.put("sessionId", cart.getSessionId());
        data.put("tableId", cart.getTable() != null ? cart.getTable().getId() : null);
        data.put("tableName", cart.getTable() != null ? cart.getTable().getName() : null);
        data.put("totalItems", cart.getTotalItems());
        data.put("totalAmount", cart.getTotalAmount());
        data.put("items", cart.getActiveCartItems().stream().map(this::toCartItemContext).toList());
        return data;
    }

    private Map<String, Object> toCartItemContext(CartItem item) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("menuItemId", item.getMenuItem() != null ? item.getMenuItem().getId() : null);
        data.put("name", item.getMenuItem() != null ? item.getMenuItem().getName() : "");
        data.put("quantity", item.getQuantity());
        data.put("unitPrice", item.getUnitPrice());
        data.put("subtotal", item.getSubtotal());
        data.put("notes", item.getNotes());
        return data;
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return UUID.fromString(value);
        } catch (Exception ignored) {
            return null;
        }
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

    private String detectLocalIntent(String message) {
        String lower = message == null ? "" : message.toLowerCase(Locale.ROOT);
        if (lower.contains("thanh toán") || lower.contains("bill")) return "PAYMENT_HELP";
        if (lower.contains("đơn") || lower.contains("tới đâu")) return "ORDER_STATUS";
        if (lower.contains("giỏ") || lower.contains("thêm món")) return "CART_HELP";
        if (lower.contains("nhân viên") || lower.contains("phục vụ")) return "CALL_STAFF";
        if (lower.contains("món") || lower.contains("ăn") || lower.contains("uống")) return "MENU_RECOMMENDATION";
        return "FAQ";
    }

    private record ChatAiResult(
            String reply,
            String intent,
            List<Map<String, Object>> suggestedItems,
            List<Map<String, Object>> actions,
            List<String> usedTools,
            boolean memoryUpdated
    ) {
        static ChatAiResult empty() {
            return new ChatAiResult(null, null, List.of(), List.of(), List.of(), false);
        }

        @SuppressWarnings("unchecked")
        static ChatAiResult from(Map<?, ?> response) {
            String reply = response.get("reply") != null ? response.get("reply").toString() : null;
            String intent = response.get("intent") != null ? response.get("intent").toString() : null;
            List<Map<String, Object>> suggestedItems = response.get("suggestedItems") instanceof List<?>
                    ? (List<Map<String, Object>>) response.get("suggestedItems")
                    : List.of();
            List<Map<String, Object>> actions = response.get("actions") instanceof List<?>
                    ? (List<Map<String, Object>>) response.get("actions")
                    : List.of();
            List<String> usedTools = response.get("usedTools") instanceof List<?>
                    ? ((List<?>) response.get("usedTools")).stream().map(Object::toString).toList()
                    : List.of();
            boolean memoryUpdated = Boolean.TRUE.equals(response.get("memoryUpdated"));
            return new ChatAiResult(reply, intent, suggestedItems, actions, usedTools, memoryUpdated);
        }
    }
}
