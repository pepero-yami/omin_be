package com.sparta.omin.app.model.store.dto;

import com.sparta.omin.app.model.store.code.Category;
import com.sparta.omin.app.model.store.code.Status;

import java.util.UUID;

public record StoreSearchResponse(
        UUID storeId,
        Category category,
        String name,
        String roadAddress,
        String detailAddress,
        //double rating,
        Status status,
        String mainImage                // 대표이미지
) {

}
