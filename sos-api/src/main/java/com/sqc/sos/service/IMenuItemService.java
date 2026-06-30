package com.sqc.sos.service;

import com.sqc.sos.dto.menuitem.MenuItemRequest;
import com.sqc.sos.dto.menuitem.MenuItemResponse;
import com.sqc.sos.dto.page.PageResponse;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface IMenuItemService {
    
    PageResponse<MenuItemResponse> getAllMenuItems(String name, Pageable pageable);
    
    PageResponse<MenuItemResponse> getActiveMenuItems(Pageable pageable);
    
    PageResponse<MenuItemResponse> getAvailableMenuItems(Pageable pageable);
    
    PageResponse<MenuItemResponse> getMenuItemsByCategory(Long categoryId, Pageable pageable);
    
    PageResponse<MenuItemResponse> searchMenuItems(String keyword, Pageable pageable);
    
    MenuItemResponse getMenuItemById(Long id);
    
    MenuItemResponse createMenuItem(MenuItemRequest request);
    
    MenuItemResponse updateMenuItem(Long id, MenuItemRequest request);
    
    void deleteMenuItem(Long id);
    
    void toggleAvailability(Long id);
    
    void toggleActiveStatus(Long id);

    // Promotions on MenuItem
    void upsertPromotion(Long menuItemId, java.math.BigDecimal promotionalPrice, java.time.LocalDateTime endDate);
    void removePromotion(Long menuItemId);
    java.util.List<com.sqc.sos.dto.menuitem.MenuItemResponse> listPromotions(int limit, java.time.LocalDateTime now);

    // Best sellers
    java.util.List<com.sqc.sos.dto.menuitem.MenuItemResponse> listBestSellers(int limit, java.time.LocalDateTime from);
} 