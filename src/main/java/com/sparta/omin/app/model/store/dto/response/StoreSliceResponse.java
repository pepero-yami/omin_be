package com.sparta.omin.app.model.store.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record StoreSliceResponse<T>(
        List<T> content,
        boolean hasNext,
        Double nextLastDistance,
        UUID nextLastId,
        LocalDateTime nextLastCreatedAt
) {
    //거리순 기반
    public static <T> StoreSliceResponse<T> ofDistanceCursor(List<T> content, boolean hasNext, Double nextLastDistance, UUID nextLastId) {
        return new StoreSliceResponse<>(content, hasNext, nextLastDistance, nextLastId, null);
    }

    //생성일 역순 기반
    public static <T> StoreSliceResponse<T> ofCreatedAtCursor(List<T> content, boolean hasNext, LocalDateTime nextLastCreatedAt, UUID nextLastId) {
        return new StoreSliceResponse<>(content, hasNext, null, nextLastId, nextLastCreatedAt);
    }
}
