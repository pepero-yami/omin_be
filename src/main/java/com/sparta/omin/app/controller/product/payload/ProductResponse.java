package com.sparta.omin.app.controller.product.payload;

import com.sparta.omin.app.model.product.code.ProductStatus;
import com.sparta.omin.app.model.product.dto.ProductResult;
import java.util.UUID;
import lombok.Builder;

@Builder
public class ProductResponse {

    private UUID id;
    private String name;
    private String description;
    private Double price;
    private ProductStatus status;

    public static ProductResponse from(ProductResult dto) {
        return ProductResponse.builder()
            .id(dto.getId())
            .name(dto.getName())
            .description(dto.getDescription())
            .price(dto.getPrice())
            .status(dto.getStatus())
            .build();
    }
}
