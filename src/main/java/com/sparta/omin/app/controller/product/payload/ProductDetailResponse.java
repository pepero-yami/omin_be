package com.sparta.omin.app.controller.product.payload;

import com.sparta.omin.app.model.product.code.ProductStatus;
import com.sparta.omin.app.model.product.dto.ProductResult;
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
    List<String> imgUrl
) {

    public static ProductDetailResponse from(ProductResult dto, List<String> imgUrl) {
        return ProductDetailResponse.builder()
            .productId(dto.id())
            .storeId(dto.storeId())
            .name(dto.name())
            .description(dto.description())
            .price(dto.price())
            .status(dto.status())
            .imgUrl(imgUrl)
            .build();
    }
}
