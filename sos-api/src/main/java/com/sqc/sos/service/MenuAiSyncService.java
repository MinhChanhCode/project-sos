package com.sqc.sos.service;

import com.sqc.sos.model.MenuItem;
import com.sqc.sos.repository.IMenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuAiSyncService {
    private final IMenuItemRepository menuItemRepository;

    @Value("${ai.service.url:}")
    private String aiServiceUrl;

    public Map<String, Object> syncMenu() {
        if (aiServiceUrl == null || aiServiceUrl.isBlank()) {
            return Map.of("synced", false, "reason", "AI service URL is not configured");
        }

        List<Map<String, Object>> items = menuItemRepository.findByIsActiveTrue().stream()
                .map(this::toAiItem)
                .toList();

        Map<String, Object> response = WebClient.create(aiServiceUrl)
                .post()
                .uri("/menu/sync")
                .bodyValue(Map.of("items", items))
                .retrieve()
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(3));

        if (response == null) {
            return Map.of("synced", false, "count", items.size(), "reason", "AI service returned empty response");
        }

        return Map.of(
                "synced", true,
                "count", items.size(),
                "aiResponse", response
        );
    }

    public void syncMenuSafely() {
        try {
            Map<String, Object> result = syncMenu();
            if (!Boolean.TRUE.equals(result.get("synced"))) {
                log.warn("Menu AI sync skipped: {}", result);
            }
        } catch (Exception ex) {
            log.warn("Menu AI sync failed: {}", ex.getMessage());
        }
    }

    private Map<String, Object> toAiItem(MenuItem item) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", item.getId());
        payload.put("name", item.getName());
        payload.put("description", item.getDescription());
        payload.put("price", item.getPrice());
        payload.put("imageUrl", item.getImageUrl());
        payload.put("categoryName", item.getCategory() != null ? item.getCategory().getName() : null);
        payload.put("isAvailable", item.getIsAvailable());
        payload.put("isActive", item.getIsActive());
        payload.put("type", item.getType());
        payload.put("tasteTags", item.getTasteTags());
        payload.put("spicyLevel", item.getSpicyLevel());
        payload.put("ingredients", item.getIngredients());
        payload.put("allergens", item.getAllergens());
        payload.put("suitableFor", item.getSuitableFor());
        payload.put("pairing", item.getPairing());
        payload.put("isVegetarian", item.getIsVegetarian());
        payload.put("prepTimeMinutes", item.getPrepTimeMinutes());
        payload.put("promotionalPrice", item.getPromotionalPrice());
        payload.put("promotionEndDate", item.getPromotionEndDate());
        return payload;
    }
}
