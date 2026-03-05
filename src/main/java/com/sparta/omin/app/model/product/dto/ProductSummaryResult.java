package com.sparta.omin.app.model.product.dto;

import com.sparta.omin.app.model.product.code.ProductStatus;
import com.sparta.omin.app.model.product.entity.Product;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/**
 * 메뉴 설명을 포함하지 않는 정보를 반환하는 객체입니다.
 */
@Data
@Builder
public class ProductSummaryResult {

    private UUID id;
    private String name;
    private Double price;
    private ProductStatus status;

    public static ProductSummaryResult from(Product product) {
        return ProductSummaryResult.builder()
            .id(product.getId())
            .name(product.getName())
            .price(product.getPrice())
            .status(product.getStatus())
            .build();
    }
}
