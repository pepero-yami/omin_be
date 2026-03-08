package com.sparta.omin.app.model.order.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record OrderCreateRequest(

        @NotNull(message = "가게 ID는 필수입니다")
        UUID storeId,

        @NotNull(message = "주소 ID는 필수입니다")
        UUID addressId,

        @Size(max = 200, message = "요청사항은 200자 이내여야 합니다")
        String userRequest,

        @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다")
        List<OrderItemRequest> orderItems
) {
    public record OrderItemRequest(

            @NotNull(message = "상품 ID는 필수입니다")
            UUID productId,

            @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
            int quantity
    ) {
    }
}