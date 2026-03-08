package com.sparta.omin.app.model.store.dto;

import com.sparta.omin.app.model.store.code.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;


public record StoreUpdateRequest(
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
        @NotEmpty
        @Size(max = 20)
        List<StoreImageRequest> images
) {
    public enum ImageAction {
        KEEP, DELETE, ADD
    }

    public record StoreImageRequest(
            UUID id,
            @NotNull
            ImageAction action
    ) {
    }
}
