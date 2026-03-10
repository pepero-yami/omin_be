package com.sparta.omin.app.model.store.dto.request;

import com.sparta.omin.app.model.store.code.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record StoreCreateRequest(
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
        String detailAddress
) {
}
