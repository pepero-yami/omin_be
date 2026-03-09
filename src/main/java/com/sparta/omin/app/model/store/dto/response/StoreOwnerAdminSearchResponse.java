package com.sparta.omin.app.model.store.dto.response;

import com.sparta.omin.app.model.store.code.Category;
import com.sparta.omin.app.model.store.code.Status;
import com.sparta.omin.app.model.store.entity.Store;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record StoreOwnerAdminSearchResponse(
        UUID id,
        String name,
        Category category,
        Status status,
        String roadAddress,
        LocalDateTime createdAt
) {
    public static StoreOwnerAdminSearchResponse of(Store store) {
        return StoreOwnerAdminSearchResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .category(store.getCategory())
                .status(store.getStatus())
                .roadAddress(store.getRoadAddress())
                .createdAt(store.getCreatedAt())
                .build();
    }
}
