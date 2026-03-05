package com.sparta.omin.app.model.product.dto;

import com.sparta.omin.app.model.product.code.ProductStatus;
import java.util.UUID;

public record ProductCreateCommand(
    UUID storeId,
    String name,
    String description,
    Double price,
    ProductStatus status,
    DescriptionGenerateOption aiOption
) {
    public record DescriptionGenerateOption(
        boolean enabled,
        String userPrompt
    ) {}
}
