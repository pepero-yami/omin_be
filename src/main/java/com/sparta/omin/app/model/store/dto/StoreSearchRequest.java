package com.sparta.omin.app.model.store.dto;

import com.sparta.omin.app.model.store.code.Category;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StoreSearchRequest(
        @NotNull
        UUID addressId,
        @NotNull
        Category category,
        String name,
        @Min(1)
        Integer size,
        @Min(0)
        Integer page,
        // TODO: 정렬 기준으로 활용 예정 (현재 거리순 고정)
        String criteria,
        Double lastDistance,
        UUID lastId

) {
    public StoreSearchRequest {
        page = page == null ? 0 : page;
        size = size == null ? 10 : size;
        criteria = criteria == null || criteria.isEmpty() ? "distance" : criteria;
    }
}
