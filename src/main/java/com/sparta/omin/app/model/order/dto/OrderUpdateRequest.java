package com.sparta.omin.app.model.order.dto;

import java.util.UUID;

public record OrderUpdateRequest(
        UUID addressId,
        String userRequest
) {
}
