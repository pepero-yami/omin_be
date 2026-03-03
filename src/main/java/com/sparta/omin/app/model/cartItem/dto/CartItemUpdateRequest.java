package com.sparta.omin.app.model.cartItem.dto;

import java.util.UUID;

public record CartItemUpdateRequest(UUID productId, int quantity) {
}
