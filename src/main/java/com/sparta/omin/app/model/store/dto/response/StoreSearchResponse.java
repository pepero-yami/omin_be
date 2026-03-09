package com.sparta.omin.app.model.store.dto.response;

import com.sparta.omin.app.model.store.code.Category;
import com.sparta.omin.app.model.store.code.Status;

import java.util.UUID;

public record StoreSearchResponse(
        UUID storeId,
        Category category,
        String name,
        String roadAddress,
        String detailAddress,
        Status status,
        double distance,
        double avgRating,
        long totalReview,
        String mainImage                // 대표이미지
) {

}
