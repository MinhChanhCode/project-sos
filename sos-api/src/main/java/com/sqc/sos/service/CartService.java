package com.sqc.sos.service;

import com.sqc.sos.dto.cart.CartItemRequest;
import com.sqc.sos.dto.cart.CartRequest;
import com.sqc.sos.dto.cart.CartResponse;
import com.sqc.sos.dto.cart.CartItemResponse;
import com.sqc.sos.dto.page.PageResponse;
import com.sqc.sos.exception.AppException;
import com.sqc.sos.exception.ErrorCode;
import com.sqc.sos.mapper.ICartMapper;
import com.sqc.sos.model.Cart;
import com.sqc.sos.model.CartItem;
import com.sqc.sos.model.MenuItem;
import com.sqc.sos.model.TableEntity;
import com.sqc.sos.repository.ICartItemRepository;
import com.sqc.sos.repository.ICartRepository;
import com.sqc.sos.repository.IMenuItemRepository;
import com.sqc.sos.repository.ITableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.context.ApplicationEventPublisher;
import com.sqc.sos.service.CartUpdatedEvent;

import java.util.Comparator;
import java.util.List;

// Thêm class event cho realtime cart update

@Service
@RequiredArgsConstructor
@Transactional
public class CartService implements ICartService {
    private final ICartRepository cartRepository;
    private final ICartItemRepository cartItemRepository;
    private final IMenuItemRepository menuItemRepository;
    private final ITableRepository tableRepository;
    private final ICartMapper cartMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public CartResponse createCart(CartRequest request) {
        // Validate table exists
        TableEntity table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_FOUND));

        // If sessionId provided and an active cart exists for it, return that cart (idempotent)
        if (request.getSessionId() != null && !request.getSessionId().isBlank()) {
            Optional<Cart> existingBySession = cartRepository.findBySessionIdAndIsActiveTrue(request.getSessionId());
            if (existingBySession.isPresent()) {
                return cartMapper.toResponse(existingBySession.get());
            }
        }

        // If no sessionId provided, try to reuse existing active cart for table
        if (request.getSessionId() == null || request.getSessionId().isBlank()) {
            Optional<Cart> existingByTable = findReusableActiveCartForTable(table.getId(), true);
            if (existingByTable.isPresent()) {
                return cartMapper.toResponse(existingByTable.get());
            }
        }

        // Create new cart, generate sessionId when missing
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = java.util.UUID.randomUUID().toString();
        }

        Cart cart = Cart.builder()
                .table(table)
                .sessionId(sessionId)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toResponse(savedCart);
    }

    @Override
    public CartResponse openCartForTable(UUID tableId) {
        // Validate table
        TableEntity table = tableRepository.findById(tableId)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_FOUND));

        // Reuse active cart if exists
        Optional<Cart> existingByTable = findReusableActiveCartForTable(tableId, true);
        if (existingByTable.isPresent()) {
            return cartMapper.toResponse(existingByTable.get());
        }

        // Create new cart with generated session
        String sessionId = java.util.UUID.randomUUID().toString();
        Cart cart = Cart.builder()
                .table(table)
                .sessionId(sessionId)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        Cart saved = cartRepository.save(cart);
        return cartMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCartBySessionId(String sessionId) {
        Cart cart = cartRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCartByTableId(UUID tableId) {
        Cart cart = findReusableActiveCartForTable(tableId, false)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        return cartMapper.toResponse(cart);
    }

    @Override
    public CartResponse addItemToCart(String sessionId, CartItemRequest request) {
        Cart cart = cartRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_FOUND));

        // Check if item already exists in cart (không phân biệt isActive)
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndMenuItemId(cart.getId(), request.getMenuItemId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            boolean wasActive = Boolean.TRUE.equals(item.getIsActive());
            item.setIsActive(true);
            if (wasActive) {
                // Nếu item đang active, cộng dồn số lượng
                item.setQuantity(item.getQuantity() + request.getQuantity());
            } else {
                // Nếu item từng bị ẩn, reset lại quantity
                item.setQuantity(request.getQuantity());
            }
            if (request.getNotes() != null) {
                item.setNotes(request.getNotes());
            }
            cartItemRepository.save(item);
            eventPublisher.publishEvent(new CartUpdatedEvent(cart.getTable().getId()));
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .menuItem(menuItem)
                    .quantity(request.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .notes(request.getNotes())
                    .isActive(true)
                    .build();
            cartItemRepository.save(newItem);
            eventPublisher.publishEvent(new CartUpdatedEvent(cart.getTable().getId()));
        }

        // Update cart timestamp
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return getCartBySessionId(sessionId);
    }

    @Override
    public CartResponse updateCartItem(String sessionId, Long menuItemId, CartItemRequest request) {
        Cart cart = cartRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        CartItem cartItem = cartItemRepository.findByCartIdAndMenuItemIdAndIsActiveTrue(cart.getId(), menuItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (request.getQuantity() != null) {
            if (request.getQuantity() <= 0) {
                // Remove item if quantity is 0 or negative
                cartItemRepository.delete(cartItem);
                eventPublisher.publishEvent(new CartUpdatedEvent(cart.getTable().getId()));
            } else {
                cartItem.setQuantity(request.getQuantity());
                cartItemRepository.save(cartItem);
                eventPublisher.publishEvent(new CartUpdatedEvent(cart.getTable().getId()));
            }
        }

        if (request.getNotes() != null) {
            cartItem.setNotes(request.getNotes());
            cartItemRepository.save(cartItem);
            eventPublisher.publishEvent(new CartUpdatedEvent(cart.getTable().getId()));
        }

        // Update cart timestamp
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return getCartBySessionId(sessionId);
    }

    @Override
    public CartResponse removeItemFromCart(String sessionId, Long menuItemId) {
        Cart cart = cartRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        CartItem cartItem = cartItemRepository.findByCartIdAndMenuItemIdAndIsActiveTrue(cart.getId(), menuItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        cartItemRepository.delete(cartItem);
        eventPublisher.publishEvent(new CartUpdatedEvent(cart.getTable().getId()));

        // Update cart timestamp
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return getCartBySessionId(sessionId);
    }

    @Override
    public CartResponse clearCart(String sessionId) {
        Cart cart = cartRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        cartItemRepository.deleteByCartId(cart.getId());
        eventPublisher.publishEvent(new CartUpdatedEvent(cart.getTable().getId()));

        // Update cart timestamp
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return getCartBySessionId(sessionId);
    }

    @Override
    public void deleteCart(String sessionId) {
        Cart cart = cartRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        cart.setIsActive(false);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    @Override
    public PageResponse<CartItemResponse> getCartItemsBySessionId(String sessionId, Pageable pageable) {
        Cart cart = cartRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        Page<CartItem> page = cartItemRepository.findByCartIdAndIsActiveTrue(cart.getId(), pageable);
        return PageResponse.from(page.map(CartItemResponse::fromEntity));
    }

    private Optional<Cart> findReusableActiveCartForTable(UUID tableId, boolean closeOlderCarts) {
        List<Cart> carts = cartRepository.findAllByTableIdAndIsActiveTrue(tableId);
        if (carts.isEmpty()) {
            return Optional.empty();
        }

        carts.sort(Comparator.comparing(
                Cart::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder())
        ).reversed());

        Cart newest = carts.get(0);
        if (closeOlderCarts && carts.size() > 1) {
            LocalDateTime now = LocalDateTime.now();
            for (int i = 1; i < carts.size(); i++) {
                Cart oldCart = carts.get(i);
                oldCart.setIsActive(false);
                oldCart.setUpdatedAt(now);
                cartRepository.save(oldCart);
            }
        }
        return Optional.of(newest);
    }
} 
