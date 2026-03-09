package com.sparta.omin.app.model.store.dto.request;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record StoreOwnerAdminSearchRequest(
        LocalDateTime lastCreatedAt,
        UUID lastId,
        Integer size
) {
    private static final Set<Integer> ALLOWED_SIZES = Set.of(10, 30, 50);

    public StoreOwnerAdminSearchRequest {
        size = (size != null && ALLOWED_SIZES.contains(size)) ? size : 10;
    }
}
