package com.sparta.omin.app.model.store.dto;

import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.store.entity.StoreImage;
import lombok.Builder;

import java.util.UUID;

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
