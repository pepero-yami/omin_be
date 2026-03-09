package com.sparta.omin.app.controller.product.payload;

import com.sparta.omin.app.model.product.code.ProductStatus;
import com.sparta.omin.app.model.product.dto.ProductDetailResult;
import com.sparta.omin.app.model.product.dto.ProductImageResult;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ProductDetailResponse(
    UUID productId,
    UUID storeId,
    String name,
    String description,
    Double price,
    ProductStatus status,
    List<ProductImageResult> images
) {

    public static ProductDetailResponse from(ProductDetailResult dto, List<ProductImageResult> images) {
        return ProductDetailResponse.builder()
            .productId(dto.id())
            .storeId(dto.storeId())
            .name(dto.name())
            .description(dto.description())
            .price(dto.price())
            .status(dto.status())
            .images(images)
            .build();
    }
}
