package com.sparta.omin.app.model.cartItem.dto;

import java.util.UUID;

public record CartItemUpdateRequest(UUID storeId, UUID productId, int quantity) {
}
