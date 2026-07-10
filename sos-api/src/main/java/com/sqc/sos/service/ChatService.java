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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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
        String message = request.getMessage() == null ? "" : request.getMessage().trim();
        if (message.length() > 600) {
            message = message.substring(0, 600);
            request.setMessage(message);
        }
        ChatAiResult aiResult = callExternalAi(request, sessionId);
        String reply = aiResult.reply();
        String intent = aiResult.intent();
        List<Map<String, Object>> suggestedItems = aiResult.suggestedItems();
        List<Map<String, Object>> actions = aiResult.actions();
        List<String> usedTools = aiResult.usedTools();
        boolean memoryUpdated = aiResult.memoryUpdated();
        boolean llmUsed = aiResult.llmUsed();
        String llmProvider = aiResult.llmProvider();
        String fallbackReason = aiResult.fallbackReason();
        String localIntent = detectLocalIntent(request.getMessage());

        if (shouldOverrideAiReply(localIntent, reply)) {
            intent = localIntent;
            reply = buildLocalReply(request, sessionId, localIntent);
            suggestedItems = List.of();
            actions = List.of();
            usedTools = List.of("sos_api_local_faq_guard");
            memoryUpdated = false;
            llmUsed = false;
            llmProvider = null;
            fallbackReason = "sos_api_overrode_generic_menu_reply";
        }

        if (reply == null || reply.isBlank()) {
            intent = intent != null ? intent : localIntent;
            reply = buildLocalReply(request, sessionId, intent);
            usedTools = List.of("local_menu_rag");
            memoryUpdated = false;
            llmUsed = false;
            fallbackReason = fallbackReason != null ? fallbackReason : "sos_api_local_rag_ai_service_unavailable";
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
                .llmUsed(llmUsed)
                .llmProvider(llmProvider)
                .fallbackReason(fallbackReason)
                .historyId(history.getId())
                .build();
    }

    private boolean shouldOverrideAiReply(String localIntent, String reply) {
        if (!Set.of("FAQ", "OUT_OF_SCOPE", "PAYMENT_HELP", "CALL_STAFF", "CART_HELP", "ORDER_STATUS").contains(localIntent)) {
            return false;
        }
        if (reply == null || reply.isBlank()) {
            return false;
        }
        return isGenericMenuReply(reply);
    }

    private boolean isGenericMenuReply(String reply) {
        String normalizedReply = normalizeSearchText(reply);
        return normalizedReply.startsWith("dua tren menu")
                || normalizedReply.contains("toi goi y")
                || normalizedReply.contains("goi y cac mon")
                || normalizedReply.contains("goi y mon")
                || normalizedReply.contains("mon phu hop tu menu")
                || normalizedReply.contains("dua tren du lieu menu");
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
        } catch (Exception ex) {
            log.warn("AI service call failed at {}: {}", aiServiceUrl, ex.getMessage());
        }
        return ChatAiResult.empty();
    }

    private Map<String, Object> buildAiContext(ChatRequest request, String sessionId) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("restaurant", Map.of(
                "name", "Bếp Mẹ Hương",
                "brand", "Gọi Món",
                "openingHours", "08:00 - 22:00 mỗi ngày",
                "serviceStyle", "Đặt món tại bàn bằng QR, nhân viên phục vụ tại bàn, bếp cập nhật trạng thái realtime",
                "payment", "Bill có QR thanh toán demo; nhân viên xác nhận thanh toán trên hệ thống"
        ));
        context.put("customerName", request.getCustomerName());
        context.put("sessionId", sessionId);
        context.put("tableId", request.getTableId());
        context.put("tableNumber", request.getTableNumber());
        List<MenuItem> activeMenu = menuItemRepository.findByIsActiveTrue();
        context.put("menu", selectRelevantMenu(activeMenu, request.getMessage()).stream().map(this::toMenuContext).toList());
        context.put("menuSummary", buildMenuSummary(activeMenu));
        context.put("history", chatHistoryRepository.findTop20BySessionIdOrderByCreatedAtDesc(sessionId).stream()
                .sorted(Comparator.comparing(ChatHistory::getCreatedAt))
                .limit(8)
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

    private List<MenuItem> selectRelevantMenu(List<MenuItem> items, String message) {
        String lower = message == null ? "" : message.toLowerCase(Locale.ROOT);
        BigDecimal maxBudget = extractBudget(lower);
        boolean wantsDrink = lower.contains("uống") || lower.contains("nước") || lower.contains("bia") || lower.contains("cà phê") || lower.contains("trà");
        boolean wantsCombo = lower.contains("combo") || lower.contains("set") || lower.contains("nhóm") || lower.contains("người");
        boolean wantsVegetarian = lower.contains("chay");
        boolean noSpicy = lower.contains("không cay") || lower.contains("khong cay") || lower.contains("ít cay");
        List<String> allergies = detectAllergies(lower);

        Comparator<MenuItem> byRelevance = Comparator
                .comparingInt((MenuItem item) -> scoreMenuItem(item, lower, wantsDrink, wantsCombo, wantsVegetarian, noSpicy, maxBudget, allergies))
                .reversed()
                .thenComparing(MenuItem::getPrice, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(MenuItem::getId);

        List<MenuItem> ranked = items.stream()
                .filter(item -> Boolean.TRUE.equals(item.getIsActive()))
                .sorted(byRelevance)
                .limit(150)
                .toList();
        return ranked.isEmpty() ? items.stream().limit(150).toList() : ranked;
    }

    private int scoreMenuItem(MenuItem item, String query, boolean wantsDrink, boolean wantsCombo,
                              boolean wantsVegetarian, boolean noSpicy, BigDecimal maxBudget, List<String> allergies) {
        String text = String.join(" ",
                safe(item.getName()),
                safe(item.getDescription()),
                item.getCategory() != null ? safe(item.getCategory().getName()) : "",
                safe(item.getType()),
                safe(item.getTasteTags()),
                safe(item.getIngredients()),
                safe(item.getAllergens()),
                safe(item.getSuitableFor()),
                safe(item.getPairing())
        ).toLowerCase(Locale.ROOT);
        int score = Boolean.TRUE.equals(item.getIsAvailable()) ? 5 : -20;
        for (String token : query.split("[^\\p{L}\\p{N}]+")) {
            if (token.length() > 1 && text.contains(token)) score += 3;
        }
        if (wantsDrink && (text.contains("đồ uống") || text.contains("nước") || text.contains("trà") || text.contains("cà phê") || "DRINK".equalsIgnoreCase(item.getType()))) score += 10;
        if (wantsCombo && (text.contains("combo") || "COMBO".equalsIgnoreCase(item.getType()))) score += 10;
        if (wantsVegetarian && Boolean.TRUE.equals(item.getIsVegetarian())) score += 10;
        if (noSpicy && (item.getSpicyLevel() == null || item.getSpicyLevel() == 0)) score += 8;
        if (maxBudget != null && item.getPrice() != null && item.getPrice().compareTo(maxBudget) <= 0) score += 6;
        for (String allergy : allergies) {
            if (text.contains(allergy)) score -= 30;
        }
        return score;
    }

    private Map<String, Object> buildMenuSummary(List<MenuItem> items) {
        Map<String, Long> byCategory = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getCategory() != null ? item.getCategory().getName() : "Khác",
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
        long available = items.stream().filter(item -> Boolean.TRUE.equals(item.getIsAvailable())).count();
        long promotional = items.stream().filter(item -> item.getPromotionalPrice() != null).count();
        return Map.of(
                "totalItems", items.size(),
                "availableItems", available,
                "promotionalItems", promotional,
                "categories", byCategory
        );
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

    private String buildLocalReply(ChatRequest request, String sessionId, String intent) {
        if ("ORDER_STATUS".equals(intent)) {
            UUID tableUuid = parseUuid(request.getTableId());
            if (tableUuid != null) {
                try {
                    TableDetailResponse table = tableQueryService.getTableDetail(tableUuid);
                    List<TableOrderItemSummaryView> items = table.getSessionItems() == null ? List.of() : table.getSessionItems().stream()
                            .map(i -> new TableOrderItemSummaryView(i.getMenuItemName(), i.getPendingQuantity(), i.getPreparingQuantity(), i.getCompletedQuantity(), i.getServedQuantity()))
                            .toList();
                    if (!items.isEmpty()) {
                        StringBuilder sb = new StringBuilder("Đơn của bàn ").append(request.getTableNumber() != null ? request.getTableNumber() : table.getTableName()).append(" hiện tại:\n");
                        for (TableOrderItemSummaryView item : items.stream().limit(6).toList()) {
                            sb.append("- ").append(item.name()).append(": ")
                                    .append(item.pending()).append(" chờ bếp, ")
                                    .append(item.preparing()).append(" đang làm, ")
                                    .append(item.ready()).append(" sẵn sàng, ")
                                    .append(item.served()).append(" đã phục vụ\n");
                        }
                        return sb.toString().trim();
                    }
                } catch (Exception ignored) {
                }
            }
            return "Mình chưa thấy đơn đang xử lý cho bàn này. Nếu bạn vừa đặt món, hãy chờ vài giây hoặc nhắn nhân viên kiểm tra giúp.";
        }
        if ("CART_HELP".equals(intent)) {
            Optional<Cart> cart = cartRepository.findBySessionIdAndIsActiveTrue(sessionId);
            if (cart.isPresent() && !cart.get().getActiveCartItems().isEmpty()) {
                Map<String, Object> data = toCartContext(cart.get());
                StringBuilder sb = new StringBuilder("Giỏ hàng của bạn đang có:\n");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
                for (Map<String, Object> item : items.stream().limit(8).toList()) {
                    sb.append("- ").append(item.get("name")).append(" x").append(item.get("quantity"))
                            .append(" - ").append(item.get("subtotal")).append("đ\n");
                }
                sb.append("Tổng tạm tính: ").append(data.get("totalAmount")).append("đ");
                return sb.toString();
            }
            return "Giỏ hàng của bạn hiện đang trống. Bạn chọn món trong menu rồi bấm thêm vào giỏ nhé.";
        }
        if ("PAYMENT_HELP".equals(intent)) {
            return "Bạn bấm Yêu cầu thanh toán để xem bill chi tiết và QR thanh toán demo. Nhân viên sẽ xác nhận thanh toán trên hệ thống.";
        }
        if ("CALL_STAFF".equals(intent)) {
            return "Bạn bấm Nhắn nhân viên hoặc Gọi dịch vụ ở góc dưới màn hình. Tin nhắn sẽ gửi đúng bàn hiện tại của bạn.";
        }
        if ("FAQ".equals(intent)) {
            return buildLocalFaqReply(request.getMessage());
        }
        if ("OUT_OF_SCOPE".equals(intent)) {
            return "Mình là trợ lý của nhà hàng Bếp Mẹ Hương. Mình có thể hỗ trợ chọn món, xem giỏ hàng, kiểm tra trạng thái món, gọi nhân viên, thanh toán và các thông tin như giờ mở cửa, wifi, hóa đơn.";
        }
        return buildLocalRagReply(request.getMessage());
    }

    private String buildLocalFaqReply(String message) {
        String lower = normalizeSearchText(message);
        if (containsAny(lower, "xin chao", "hello", "hi", "chao ban")) {
            return "Xin chào! Mình có thể tư vấn món ăn, kiểm tra giỏ hàng, xem trạng thái món, gọi nhân viên, hỗ trợ thanh toán hoặc trả lời thông tin nhà hàng.";
        }
        if (containsAny(lower, "mo cua", "gio mo cua", "may gio", "thoi gian", "open", "dong cua")) {
            return "Nhà hàng mở cửa từ 8:00 đến 22:00 mỗi ngày. Nếu bạn cần hỗ trợ ngoài giờ, hãy nhắn nhân viên trên màn hình đặt món.";
        }
        if (containsAny(lower, "dia chi", "o dau", "vi tri", "address")) {
            return "Nhà hàng ở 30 Trần Quang Diệu, Quy Nhơn, Bình Định.";
        }
        if (containsAny(lower, "wifi", "mat khau wifi", "password wifi")) {
            return "Bạn vui lòng nhắn nhân viên trên màn hình hoặc hỏi trực tiếp nhân viên để được cung cấp wifi của nhà hàng.";
        }
        if (containsAny(lower, "xuat hoa don", "invoice", "hoa don do", "hoa don vat")) {
            return "Bạn có thể yêu cầu nhân viên hỗ trợ xuất hóa đơn. Bill trên hệ thống có danh sách món, số lượng, đơn giá và tổng tiền.";
        }
        if (containsAny(lower, "huy mon", "doi mon", "cancel")) {
            return "Nếu món chưa được bếp xử lý, bạn hãy nhắn nhân viên ngay để được hỗ trợ hủy hoặc đổi món. Khi món đã chế biến, nhà hàng có thể không hỗ trợ hủy.";
        }
        if (containsAny(lower, "phi dich vu", "service fee", "giam gia", "khuyen mai")) {
            return "Nếu có phí dịch vụ, giảm giá hoặc khuyến mãi, bill sẽ hiển thị rõ trước khi thanh toán. Bạn cũng có thể nhắn nhân viên để xác nhận thêm.";
        }
        if (containsAny(lower, "danh gia", "review", "phan hoi")) {
            return "Sau khi dùng bữa, bạn có thể gửi đánh giá trên màn hình khách hàng để nhà hàng cải thiện chất lượng phục vụ.";
        }
        if (containsAny(lower, "gop y", "khieu nai", "phan anh")) {
            return "Bạn có thể nhắn nhân viên ngay trên màn hình đặt món để góp ý hoặc phản ánh. Nhân viên sẽ nhận thông báo theo đúng bàn của bạn.";
        }
        return "Mình có thể trả lời thông tin nhà hàng như giờ mở cửa, địa chỉ, wifi, hóa đơn, thanh toán, gọi nhân viên hoặc trạng thái món. Bạn muốn hỏi phần nào?";
    }

    private String buildLocalRagReply(String message) {
        String lower = normalizeSearchText(message);
        List<MenuItem> items = menuItemRepository.findAll().stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsActive()))
                .collect(Collectors.toList());

        BigDecimal maxBudget = extractBudget(lower);
        boolean wantsAvailability = containsAny(lower, "con khong", "het chua", "con mon", "het mon", "con", "het");
        boolean wantsPrice = containsAny(lower, "gia", "bao nhieu tien", "may tien", "bao nhieu", "duoi");
        boolean noSpicy = containsAny(lower, "khong cay", "it cay", "not spicy");

        List<MenuItem> matched = items.stream()
                .filter(m -> {
                    BigDecimal price = effectivePrice(m);
                    if (!wantsAvailability && !Boolean.TRUE.equals(m.getIsAvailable())) return false;
                    if (maxBudget != null && price != null && price.compareTo(maxBudget) > 0) return false;
                    if (noSpicy && m.getSpicyLevel() != null && m.getSpicyLevel() > 0) return false;
                    String itemText = normalizeSearchText(String.join(" ",
                            safe(m.getName()),
                            safe(m.getDescription()),
                            m.getCategory() != null ? safe(m.getCategory().getName()) : "",
                            safe(m.getType()),
                            safe(m.getTasteTags()),
                            safe(m.getIngredients()),
                            safe(m.getSuitableFor()),
                            safe(m.getPairing())
                    ));
                    String itemName = normalizeSearchText(m.getName());
                    if (!itemName.isBlank() && (lower.contains(itemName) || itemName.contains(lower))) return true;
                    if (lower.contains("pho")) return itemName.contains("pho");
                    if (lower.contains("combo")) return itemText.contains("combo");
                    if (containsAny(lower, "do uong", "nuoc uong", "tra", "ca phe", "sinh to", "nuoc ep")) {
                        return itemText.contains("do uong") || itemText.contains("nuoc") || itemText.contains("tra")
                                || itemText.contains("ca phe") || "DRINK".equalsIgnoreCase(m.getType());
                    }
                    if (noSpicy || maxBudget != null || wantsPrice || wantsAvailability) return true;
                    return Arrays.stream(lower.split(" "))
                            .filter(token -> token.length() > 1)
                            .anyMatch(itemText::contains);
                })
                .sorted(Comparator
                        .comparing((MenuItem m) -> Boolean.TRUE.equals(m.getIsAvailable()) ? 0 : 1)
                        .thenComparing(m -> effectivePrice(m), Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(5)
                .toList();

        if (matched.isEmpty() && !wantsPrice && !wantsAvailability && maxBudget == null && !noSpicy) {
            matched = items.stream().limit(3).toList();
        }

        if (matched.isEmpty()) {
            return "Xin chào! Hiện chưa có món trong menu. Vui lòng hỏi nhân viên để được tư vấn.";
        }

        StringBuilder sb = new StringBuilder(wantsPrice || wantsAvailability
                ? "Mình tìm thấy thông tin món trong menu hiện tại:\n"
                : "Dựa trên menu nhà hàng, tôi gợi ý:\n");
        for (MenuItem m : matched) {
            sb.append("- ").append(m.getName())
                    .append(" (").append(formatMoney(effectivePrice(m))).append(")")
                    .append(Boolean.TRUE.equals(m.getIsAvailable()) ? " - còn món" : " - hiện hết món");
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

    private BigDecimal effectivePrice(MenuItem item) {
        return item.getPromotionalPrice() != null ? item.getPromotionalPrice() : item.getPrice();
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "chưa cập nhật giá";
        return String.format("%,.0fđ", value);
    }

    private String detectLocalIntent(String message) {
        String lower = normalizeSearchText(message);
        if (containsAny(lower, "thanh toan", "bill", "qr thanh toan", "chuyen khoan", "tinh tien")) return "PAYMENT_HELP";
        if (containsAny(lower, "don cua toi", "mon cua toi", "toi dau", "ra chua", "cho lau", "trang thai don")) return "ORDER_STATUS";
        if (containsAny(lower, "gio hang", "cart", "them mon", "xoa mon", "dat mon")) return "CART_HELP";
        if (containsAny(lower, "nhan vien", "phuc vu", "ho tro", "goi phuc vu")) return "CALL_STAFF";
        if (containsAny(lower, "mo cua", "gio mo cua", "may gio", "dia chi", "wifi", "xuat hoa don", "huy mon", "doi mon", "phi dich vu", "danh gia", "review", "khieu nai")) return "FAQ";
        if (isMenuQuestion(lower)) return "MENU_RECOMMENDATION";
        if (messageMentionsMenuItem(lower)) return "MENU_RECOMMENDATION";
        if (containsAny(lower, "bitcoin", "chung khoan", "code", "lap trinh", "chinh tri", "bong da", "thoi tiet")) return "OUT_OF_SCOPE";
        return "FAQ";
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) return true;
        }
        return false;
    }

    private boolean isMenuQuestion(String lower) {
        return containsAny(lower,
                "mon an", "do an", "do uong", "nuoc uong", "combo", "khong cay", "it cay",
                "an chay", "di ung", "ngan sach", "goi y", "recommend", "gia", "gia bao nhieu",
                "bao nhieu tien", "may tien", "duoi", "con khong", "het chua", "con mon", "het mon",
                "ban chay", "ngon nhat", "mon nao", "hai san", "thit", "rau", "com", "pho", "bun", "lau",
                "tra", "ca phe", "sinh to", "nuoc ep")
                || lower.startsWith("mon ")
                || lower.endsWith(" mon")
                || lower.contains(" mon ")
                || lower.startsWith("an ")
                || lower.contains(" an gi")
                || lower.contains(" nen an")
                || lower.contains(" uong gi")
                || lower.contains(" muon an")
                || lower.contains(" muon uong");
    }

    private boolean messageMentionsMenuItem(String lower) {
        if (lower.length() < 3) return false;
        return menuItemRepository.findByIsActiveTrue().stream()
                .map(item -> normalizeSearchText(item.getName()))
                .filter(name -> name.length() >= 3)
                .anyMatch(name -> lower.contains(name) || name.contains(lower));
    }

    private String normalizeSearchText(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd');
        return normalized.replaceAll("[^\\p{Alnum}]+", " ").trim();
    }

    private List<String> detectAllergies(String lower) {
        List<String> allergies = new ArrayList<>();
        for (String term : List.of("hải sản", "hai san", "sữa", "sua", "đậu phộng", "dau phong", "trứng", "trung", "bò", "bo", "gà", "ga")) {
            if (lower.contains(term) && (lower.contains("dị ứng") || lower.contains("di ung") || lower.contains("không ăn") || lower.contains("khong an") || lower.contains("tránh"))) {
                allergies.add(term);
            }
        }
        return allergies;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record TableOrderItemSummaryView(String name, Integer pending, Integer preparing, Integer ready, Integer served) {
        TableOrderItemSummaryView {
            pending = pending == null ? 0 : pending;
            preparing = preparing == null ? 0 : preparing;
            ready = ready == null ? 0 : ready;
            served = served == null ? 0 : served;
        }
    }

    private record ChatAiResult(
            String reply,
            String intent,
            List<Map<String, Object>> suggestedItems,
            List<Map<String, Object>> actions,
            List<String> usedTools,
            boolean memoryUpdated,
            boolean llmUsed,
            String llmProvider,
            String fallbackReason
    ) {
        static ChatAiResult empty() {
            return new ChatAiResult(null, null, List.of(), List.of(), List.of(), false, false, null, null);
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
            boolean llmUsed = Boolean.TRUE.equals(response.get("llmUsed"));
            String llmProvider = response.get("llmProvider") != null ? response.get("llmProvider").toString() : null;
            String fallbackReason = response.get("fallbackReason") != null ? response.get("fallbackReason").toString() : null;
            return new ChatAiResult(reply, intent, suggestedItems, actions, usedTools, memoryUpdated, llmUsed, llmProvider, fallbackReason);
        }
    }
}
