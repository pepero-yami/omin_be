package com.sparta.omin.app.model.store.dto;

import com.sparta.omin.app.model.store.code.Category;
import com.sparta.omin.app.model.store.code.Status;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.store.entity.StoreImage;
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
        String name,
        String roadAddress,
        String detailAddress,
        Status status,
        BigDecimal longitude,
        BigDecimal latitude,
        List<StoreImageResponse> images
) {
    @Builder
    public record StoreImageResponse(
            UUID id,
            String imageUrl,
            Integer sequence
    ) {
        public static StoreImageResponse of(StoreImage storeImage) {
            return StoreImageResponse.builder()
                    .id(storeImage.getId())
                    .imageUrl(storeImage.getImageUrl())
                    .sequence(storeImage.getSequence())
                    .build();
        }
    }

    public static StoreResponse of(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .ownerId(store.getOwnerId())
                .regionId(store.getRegionId())
                .category(store.getCategory())
                .name(store.getName())
                .roadAddress(store.getRoadAddress())
                .detailAddress(store.getDetailAddress())
                .status(store.getStatus())
                .longitude(store.getLongitude())
                .latitude(store.getLatitude())
                .images(store.getImages().stream().map(StoreResponse.StoreImageResponse::of).toList())
                .build();
    }

}
