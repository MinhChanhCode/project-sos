package com.sqc.sos.service;

import com.sqc.sos.dto.cart.CartItemRequest;
import com.sqc.sos.dto.cart.CartRequest;
import com.sqc.sos.dto.cart.CartResponse;
import com.sqc.sos.dto.cart.CartItemResponse;
import com.sqc.sos.dto.page.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ICartService {

    CartResponse createCart(CartRequest request);

    // Idempotent: get existing active cart by table or create a new one
    CartResponse openCartForTable(UUID tableId);

    CartResponse getCartBySessionId(String sessionId);

    CartResponse getCartByTableId(UUID tableId);

    CartResponse addItemToCart(String sessionId, CartItemRequest request);

    CartResponse updateCartItem(String sessionId, Long menuItemId, CartItemRequest request);

    CartResponse removeItemFromCart(String sessionId, Long menuItemId);

    CartResponse clearCart(String sessionId);

    void deleteCart(String sessionId);


    PageResponse<CartItemResponse> getCartItemsBySessionId(String sessionId, Pageable pageable);
} 