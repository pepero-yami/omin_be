package com.sparta.omin.app.model.store.dto.response;

import com.sparta.omin.app.model.store.code.Category;
import com.sparta.omin.app.model.store.code.Status;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.store.entity.StoreImage;
import lombok.Builder;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Builder
public record StoreResponse(
        UUID id,
        UUID ownerId,
        Category category,
        String name,
        String roadAddress,
        String detailAddress,
        Status status,
        double avgRating,
        long totalReview,
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
                    .imageUrl(storeImage.getUrl())
                    .sequence(storeImage.getSequence())
                    .build();
        }
    }

    public static StoreResponse of(Store store) {
        return of(store, 0.0, 0L);
    }

    public static StoreResponse of(Store store, double avgRating, long totalReview) {
        return StoreResponse.builder()
                .id(store.getId())
                .ownerId(store.getOwnerId())
                .category(store.getCategory())
                .name(store.getName())
                .roadAddress(store.getRoadAddress())
                .detailAddress(store.getDetailAddress())
                .status(store.getStatus())
                .avgRating(avgRating)
                .totalReview(totalReview)
                .images(store.getImages().stream()
                        .sorted(Comparator.comparingInt(StoreImage::getSequence))
                        .map(StoreImageResponse::of).toList())
                .build();
    }

}
