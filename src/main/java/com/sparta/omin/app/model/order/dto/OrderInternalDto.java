package com.sparta.omin.app.model.order.dto;

import java.util.UUID;

public record OrderInternalDto(
        UUID orderId,
        UUID userId,
        double totalPrice
) {}