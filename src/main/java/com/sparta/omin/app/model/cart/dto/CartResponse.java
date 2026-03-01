package com.sparta.omin.app.model.cart.dto;

import java.util.UUID;

public record CartResponse(UUID userId,
                           UUID storeId
) {}
