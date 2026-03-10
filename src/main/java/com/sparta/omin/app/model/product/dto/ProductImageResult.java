package com.sparta.omin.app.model.product.dto;

import com.sparta.omin.app.model.product.entity.ProductImage;
import java.util.UUID;

public record ProductImageResult(
    UUID imageId,
    String imageUrl,
    Boolean isPrimary,
    Integer sortOrder
) {
    public static ProductImageResult from(ProductImage productImage) {
        return new ProductImageResult(
            productImage.getId(),
            productImage.getUrl(),
            productImage.getIsPrimary(),
            productImage.getSortOrder()
        );
    }
}
