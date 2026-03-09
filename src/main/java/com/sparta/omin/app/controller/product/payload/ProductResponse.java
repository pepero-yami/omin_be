package com.sparta.omin.app.controller.product.payload;

import com.sparta.omin.app.model.product.code.ProductStatus;
import com.sparta.omin.app.model.product.dto.ProductWithImageResult;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ProductResponse(
    UUID productId,
    UUID storeId,
    String name,
    String description,
    Double price,
    ProductStatus status,
    String primaryImgUrl
) {

    public static ProductResponse from(ProductWithImageResult dto) {
        return ProductResponse.builder()
            .productId(dto.id())
            .storeId(dto.storeId())
            .name(dto.name())
            .description(dto.description())
            .price(dto.price())
            .status(dto.status())
            .primaryImgUrl(dto.imgUrl())
            .build();
    }
}
