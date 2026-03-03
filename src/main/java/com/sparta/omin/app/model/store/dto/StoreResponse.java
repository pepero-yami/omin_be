package com.sparta.omin.app.model.store.dto;

import com.sparta.omin.app.model.store.entity.Category;
import com.sparta.omin.app.model.store.entity.Store;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record StoreResponse(
        UUID id,
        UUID ownerId,
        UUID regionId,
        Category category,
        String roadAddress,
        String detailAddress,
        BigDecimal longitude,
        BigDecimal latitude,
        List<StoreImageResponse> images
){
    public static StoreResponse of(Store store){
        return StoreResponse.builder()
                .id(store.getId())
                .ownerId(store.getOwnerId())
                .regionId(store.getRegionId())
                .category(store.getCategory())
                .roadAddress(store.getRoadAddress())
                .detailAddress(store.getDetailAddress())
                .longitude(store.getLongitude())
                .latitude(store.getLatitude())
                .images(store.getImages().stream().map(StoreImageResponse::of).toList())
                .build();
    }

}
