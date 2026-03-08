package com.sparta.omin.app.model.store.dto;

import java.util.List;

public record StoreListPageResponse(
        List<StoreListResponse> content,
        boolean hasNext,
        int page
) {
}
