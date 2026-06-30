package com.sqc.sos.controller;

import com.sqc.sos.dto.ApiResponse;
import com.sqc.sos.dto.menuitem.MenuItemRequest;
import com.sqc.sos.dto.menuitem.MenuItemResponse;
import com.sqc.sos.service.IMenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sqc.sos.dto.page.PageResponse;
import org.springframework.data.domain.Pageable;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

@Slf4j
@RestController
@RequestMapping("/api/v1/menu-items")
@RequiredArgsConstructor
public class MenuItemController {
    
    private final IMenuItemService menuItemService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MenuItemResponse>>> getAllMenuItems(
            @RequestParam(defaultValue = "") String name,
            Pageable pageable) {
        log.info("GET /menu-items - name: {}, page: {}", name, pageable.getPageNumber());
        PageResponse<MenuItemResponse> page = menuItemService.getAllMenuItems(name, pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Lấy danh sách món ăn thành công"));
    }
    
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PageResponse<MenuItemResponse>>> getActiveMenuItems(Pageable pageable) {
        log.info("GET /menu-items/active - page: {}", pageable.getPageNumber());
        PageResponse<MenuItemResponse> page = menuItemService.getActiveMenuItems(pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Lấy danh sách món ăn đang hoạt động thành công"));
    }
    
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<PageResponse<MenuItemResponse>>> getAvailableMenuItems(Pageable pageable) {
        log.info("GET /menu-items/available - page: {}", pageable.getPageNumber());
        PageResponse<MenuItemResponse> page = menuItemService.getAvailableMenuItems(pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Lấy danh sách món ăn có sẵn thành công"));
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PageResponse<MenuItemResponse>>> getMenuItemsByCategory(
            @PathVariable Long categoryId,
            Pageable pageable) {
        log.info("GET /menu-items/category/{} - page: {}", categoryId, pageable.getPageNumber());
        PageResponse<MenuItemResponse> page = menuItemService.getMenuItemsByCategory(categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Lấy danh sách món ăn theo danh mục thành công"));
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<MenuItemResponse>>> searchMenuItems(
            @RequestParam String keyword,
            Pageable pageable) {
        log.info("GET /menu-items/search - keyword: {}, page: {}", keyword, pageable.getPageNumber());
        PageResponse<MenuItemResponse> page = menuItemService.searchMenuItems(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Tìm kiếm món ăn thành công"));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> getMenuItemById(@PathVariable Long id) {
        log.info("GET /menu-items/{}", id);
        MenuItemResponse menuItem = menuItemService.getMenuItemById(id);
        return ResponseEntity.ok(ApiResponse.success(menuItem, "Lấy thông tin món ăn thành công"));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<MenuItemResponse>> createMenuItem(@RequestBody MenuItemRequest request) {
        log.info("POST /menu-items - request: {}", request);
        MenuItemResponse menuItem = menuItemService.createMenuItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(menuItem, "Tạo món ăn thành công"));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateMenuItem(@PathVariable Long id, @RequestBody MenuItemRequest request) {
        log.info("PUT /menu-items/{} - request: {}", id, request);
        MenuItemResponse menuItem = menuItemService.updateMenuItem(id, request);
        return ResponseEntity.ok(ApiResponse.success(menuItem, "Cập nhật món ăn thành công"));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable Long id) {
        log.info("DELETE /menu-items/{}", id);
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa món ăn thành công"));
    }
    
    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<ApiResponse<Void>> toggleAvailability(@PathVariable Long id) {
        log.info("PATCH /menu-items/{}/toggle-availability", id);
        menuItemService.toggleAvailability(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Cập nhật trạng thái có sẵn thành công"));
    }
    
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Void>> toggleActiveStatus(@PathVariable Long id) {
        log.info("PATCH /menu-items/{}/toggle-active", id);
        menuItemService.toggleActiveStatus(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Cập nhật trạng thái hoạt động thành công"));
    }

    @PatchMapping("/{id}/out-of-stock")
    public ResponseEntity<ApiResponse<Void>> markOutOfStock(@PathVariable Long id) {
        log.info("PATCH /menu-items/{}/out-of-stock", id);
        // Đơn giản: đặt isAvailable=false
        menuItemService.toggleAvailability(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Đánh dấu hết món thành công"));
    }

    // ===== Promotions embedded in MenuItem =====
    @PostMapping("/promotions")
    public ResponseEntity<ApiResponse<Void>> upsertPromotion(@RequestBody UpsertPromotionRequest request) {
        log.info("POST /menu-items/promotions - request: {}", request);
        menuItemService.upsertPromotion(request.getMenuItemId(), request.getPromotionalPrice(), request.getEndDate());
        return ResponseEntity.ok(ApiResponse.success(null, "Cập nhật khuyến mãi thành công"));
    }

    @DeleteMapping("/promotions/{menuItemId}")
    public ResponseEntity<ApiResponse<Void>> removePromotion(@PathVariable Long menuItemId) {
        log.info("DELETE /menu-items/promotions/{}", menuItemId);
        menuItemService.removePromotion(menuItemId);
        return ResponseEntity.ok(ApiResponse.success(null, "Bỏ khuyến mãi thành công"));
    }

    @GetMapping("/promotions")
    public ResponseEntity<?> getPromotions(@RequestParam(defaultValue = "8") int limit,
                                           @RequestParam(required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime now) {
        log.info("GET /menu-items/promotions - limit: {}, now: {}", limit, now);
        return ResponseEntity.ok(menuItemService.listPromotions(limit, now));
    }

    @GetMapping("/best-sellers")
    public ResponseEntity<?> getBestSellers(@RequestParam(defaultValue = "8") int limit,
                                            @RequestParam(required = false)
                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from) {
        log.info("GET /menu-items/best-sellers - limit: {}, from: {}", limit, from);
        return ResponseEntity.ok(menuItemService.listBestSellers(limit, from));
    }

    @lombok.Data
    public static class UpsertPromotionRequest {
        Long menuItemId;
        BigDecimal promotionalPrice;
        LocalDateTime endDate;
    }
} 