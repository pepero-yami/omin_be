package com.sparta.omin.app.model.store.dto;

import com.sparta.omin.app.model.store.entity.Category;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


public record StoreUpdateRequest(
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
        BigDecimal latitude,
        @NotEmpty
        @Size(max = 10)
        List<StoreImageRequest> images
) {
    public record StoreImageRequest(
            UUID id,
            boolean isNewUploaded
    ) {

    }
}