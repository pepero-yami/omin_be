package com.sparta.omin.app.model.product.dto;

import com.sparta.omin.app.model.product.code.ProductStatus;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ProductWithImageResult(
    UUID id,
    UUID storeId,
    String name,
    String description,
    Double price,
    ProductStatus status,
    String imgUrl
) { }
