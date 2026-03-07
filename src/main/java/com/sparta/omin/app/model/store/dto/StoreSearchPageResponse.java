package com.sparta.omin.app.model.store.dto;

import java.util.List;
import java.util.UUID;

public record StoreSearchPageResponse(
        List<StoreSearchResponse> content,
        boolean hasNext,
        Double nextLastDistance,
        UUID nextLastId
) {
}
