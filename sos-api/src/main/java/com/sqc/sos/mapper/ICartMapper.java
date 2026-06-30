package com.sqc.sos.mapper;

import com.sqc.sos.dto.cart.CartItemResponse;
import com.sqc.sos.dto.cart.CartResponse;
import com.sqc.sos.model.Cart;
import com.sqc.sos.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ICartMapper {

    @Mapping(target = "tableId", source = "table.id")
    @Mapping(target = "tableName", source = "table.name")
    @Mapping(target = "items", source = "activeCartItems")
    @Mapping(target = "totalItems", expression = "java(cart.getTotalItems())")
    @Mapping(target = "totalAmount", expression = "java(cart.getTotalAmount())")
    CartResponse toResponse(Cart cart);

    @Mapping(target = "menuItemId", source = "menuItem.id")
    @Mapping(target = "menuItemName", source = "menuItem.name")
    @Mapping(target = "menuItemImageUrl", source = "menuItem.imageUrl")
    @Mapping(target = "subtotal", expression = "java(cartItem.getSubtotal())")
    CartItemResponse toCartItemResponse(CartItem cartItem);

    List<CartItemResponse> toCartItemResponseList(List<CartItem> cartItems);
} 