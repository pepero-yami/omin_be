package com.sparta.omin.app.model.store.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record StoreListPageRequest(
        @Min(0)
        Integer page,
        @Min(1) @Max(100)
        Integer size
) {
    public StoreListPageRequest {
        page = page == null ? 0 : page;
        size = size == null ? 10 : size;
    }
}
