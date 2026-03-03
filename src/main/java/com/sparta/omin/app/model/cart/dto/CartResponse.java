package com.sparta.omin.app.model.cart.dto;

import com.sparta.omin.app.model.cart.entity.Cart;
import com.sparta.omin.app.model.cartItem.dto.CartItemResponse;

import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID cartId,
        UUID userId,
        UUID storeId,
        List<CartItemResponse> cartItems
) {
    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getCartItems().stream()
                .map(CartItemResponse::from)
                .toList();

        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                cart.getStoreId(),
                items
        );
    }
}
