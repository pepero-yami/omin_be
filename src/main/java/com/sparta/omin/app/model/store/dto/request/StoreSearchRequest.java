package com.sparta.omin.app.model.store.dto.request;

import com.sparta.omin.app.model.store.code.Category;
import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record StoreSearchRequest(
        @NotNull
        UUID addressId,
        @NotNull
        Category category,
        String name,
        Integer size,
        // TODO: 정렬 기준으로 활용 예정 (현재 거리순 고정)
        String criteria,
        Double lastDistance,
        UUID lastId
) {
    private static final Set<Integer> ALLOWED_SIZES = Set.of(10, 30, 50);

    public StoreSearchRequest {
        size = (size != null && ALLOWED_SIZES.contains(size)) ? size : 10;
        //거리순
        criteria = criteria == null || criteria.isEmpty() ? "distance" : criteria;
    }
}
