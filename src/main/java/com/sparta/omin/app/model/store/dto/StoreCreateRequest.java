package com.sparta.omin.app.model.store.dto;

import com.sparta.omin.app.model.store.entity.Category;
import com.sparta.omin.app.model.store.entity.Store;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;


public record StoreCreateRequest(
        @NotNull
        UUID regionId,
        @NotNull
        Category category,
        @NotBlank
        @Size(max = 100)
        String name,
        @NotBlank
        @Size(max = 255)
        String roadAddress,
        @NotBlank
        @Size(max = 100)
        String detailAddress,
        @NotNull
        @Digits(integer = 4, fraction = 6)
        BigDecimal longitude,
        @NotNull
        @Digits(integer = 4, fraction = 6)
        BigDecimal latitude
) {
    public Store toEntity(UUID ownerId) {
        return Store.builder()
                .ownerId(ownerId)
                .regionId(regionId)
                .category(category)
                .name(name)
                .roadAddress(roadAddress)
                .detailAddress(detailAddress)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
