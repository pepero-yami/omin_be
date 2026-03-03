package com.sparta.omin.app.model.cart.dto;

import java.util.UUID;

public record CartCreateRequest(
        UUID userId,      // TODO: JWT 붙으면 SecurityContext에서 추출로 교체
        UUID storeId,
        UUID productId,
        int quantity
) {}