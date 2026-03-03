package com.sparta.omin.app.model.cart.dto;

import java.util.UUID;

public record CartCreateRequest(
        UUID storeId,
        UUID productId,
        int quantity
) {}