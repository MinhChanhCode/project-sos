package com.sqc.sos.controller;

import com.sqc.sos.dto.ApiResponse;
import com.sqc.sos.dto.cart.CartItemRequest;
import com.sqc.sos.dto.cart.CartRequest;
import com.sqc.sos.dto.cart.CartResponse;
import com.sqc.sos.dto.cart.CartItemResponse;
import com.sqc.sos.dto.page.PageResponse;
import com.sqc.sos.service.ICartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;

    // Tạo giỏ hàng mới cho bàn
    @PostMapping
    public ResponseEntity<ApiResponse<CartResponse>> createCart(@RequestBody CartRequest request) {
        log.info("POST /carts - request: {}", request);
        CartResponse cart = cartService.createCart(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(cart, "Tạo giỏ hàng thành công"));
    }

    // Lấy thông tin giỏ hàng theo session ID
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<CartResponse>> getCartBySessionId(@PathVariable String sessionId) {
        log.info("GET /carts/session/{}", sessionId);
        CartResponse cart = cartService.getCartBySessionId(sessionId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Lấy thông tin giỏ hàng thành công"));
    }

    // Lấy danh sách cart items theo sessionId (có phân trang)
    @GetMapping("/session/{sessionId}/items")
    public ResponseEntity<ApiResponse<PageResponse<CartItemResponse>>> getCartItemsBySessionId(
            @PathVariable String sessionId,
            Pageable pageable) {
        log.info("GET /carts/session/{}/items - page: {}", sessionId, pageable.getPageNumber());
        PageResponse<CartItemResponse> page = cartService.getCartItemsBySessionId(sessionId, pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Lấy danh sách món trong giỏ hàng thành công"));
    }

    // Lấy thông tin giỏ hàng theo bàn
    @GetMapping("/table/{tableId}")
    public ResponseEntity<ApiResponse<CartResponse>> getCartByTableId(@PathVariable UUID tableId) {
        log.info("GET /carts/table/{}", tableId);
        CartResponse cart = cartService.getCartByTableId(tableId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Lấy thông tin giỏ hàng theo bàn thành công"));
    }

    // Idempotent mở giỏ cho bàn: nếu có thì trả về, nếu không sẽ tạo mới (dùng cho FE khi chỉ có tableId)
    @PostMapping("/table/{tableId}/open")
    public ResponseEntity<ApiResponse<CartResponse>> openCartForTable(@PathVariable UUID tableId) {
        log.info("POST /carts/table/{}/open", tableId);
        CartResponse cart = cartService.openCartForTable(tableId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(cart, "Mở giỏ hàng cho bàn thành công"));
    }

    // Thêm món vào giỏ hàng
    @PostMapping("/session/{sessionId}/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItemToCart(
            @PathVariable String sessionId,
            @RequestBody CartItemRequest request) {
        log.info("POST /carts/session/{}/items - request: {}", sessionId, request);
        CartResponse cart = cartService.addItemToCart(sessionId, request);
        return ResponseEntity.ok(ApiResponse.success(cart, "Thêm món vào giỏ hàng thành công"));
    }

    // Cập nhật số lượng món trong giỏ hàng
    @PutMapping("/session/{sessionId}/items/{menuItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @PathVariable String sessionId,
            @PathVariable Long menuItemId,
            @RequestBody CartItemRequest request) {
        log.info("PUT /carts/session/{}/items/{} - request: {}", sessionId, menuItemId, request);
        CartResponse cart = cartService.updateCartItem(sessionId, menuItemId, request);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cập nhật món trong giỏ hàng thành công"));
    }

    // Xóa món khỏi giỏ hàng
    @DeleteMapping("/session/{sessionId}/items/{menuItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItemFromCart(
            @PathVariable String sessionId,
            @PathVariable Long menuItemId) {
        log.info("DELETE /carts/session/{}/items/{}", sessionId, menuItemId);
        CartResponse cart = cartService.removeItemFromCart(sessionId, menuItemId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Xóa món khỏi giỏ hàng thành công"));
    }

    // Xóa toàn bộ giỏ hàng
    @DeleteMapping("/session/{sessionId}/clear")
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(@PathVariable String sessionId) {
        log.info("DELETE /carts/session/{}/clear", sessionId);
        CartResponse cart = cartService.clearCart(sessionId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Xóa toàn bộ giỏ hàng thành công"));
    }

    // Xóa giỏ hàng (deactivate)
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> deleteCart(@PathVariable String sessionId) {
        log.info("DELETE /carts/session/{}", sessionId);
        cartService.deleteCart(sessionId);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa giỏ hàng thành công"));
    }


    // API để chọn bàn (tạo link QR)
    @GetMapping("/table/{tableId}/qr")
    public ResponseEntity<ApiResponse<String>> getTableQR(@PathVariable UUID tableId) {
        log.info("GET /carts/table/{}/qr", tableId);
        String qrUrl = "http://localhost:8080/api/v1/carts/table/" + tableId;
        return ResponseEntity.ok(ApiResponse.success(qrUrl, "Link QR cho bàn"));
    }
} 