package com.sparta.omin.app.model.product.dto;

import com.sparta.omin.app.model.product.code.ProductStatus;
import com.sparta.omin.app.model.product.entity.Product;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ProductDetailResult(
    UUID id,
    UUID storeId,
    String name,
    String description,
    Double price,
    ProductStatus status
) {

    public static ProductDetailResult from(Product product) {
        return ProductDetailResult.builder()
            .id(product.getId())
            .storeId(product.getStore().getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .status(product.getStatus())
            .build();
    }
}
