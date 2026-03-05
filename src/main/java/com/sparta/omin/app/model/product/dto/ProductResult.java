package com.sparta.omin.app.model.product.dto;

import com.sparta.omin.app.model.product.code.ProductStatus;
import com.sparta.omin.app.model.product.entity.Product;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResult {
    private UUID id;
    private String name;
    private String description;
    private Double price;
    private ProductStatus status;

    public static ProductResult from(Product product) {
        return ProductResult.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .status(product.getStatus())
            .build();
    }
}
