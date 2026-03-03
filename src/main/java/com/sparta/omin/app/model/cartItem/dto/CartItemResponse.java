package com.sparta.omin.app.model.cartItem.dto;

import com.sparta.omin.app.model.cart.dto.CartResponse;
import com.sparta.omin.app.model.cartItem.entity.CartItem;

import java.util.UUID;

public record CartItemResponse(UUID cartItemId, UUID productId, int quantity) {
    public static CartItemResponse from(CartItem cartItem) {
        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getProduct().getId(),
                cartItem.getQuantity()
        );
    }
}
