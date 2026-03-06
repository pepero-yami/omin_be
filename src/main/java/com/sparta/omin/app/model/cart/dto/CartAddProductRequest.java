package com.sparta.omin.app.model.cart.dto;

import jakarta.validation.constraints.Min;
import java.util.UUID;

public record CartAddProductRequest (
	UUID productId,
	UUID storeId,
	@Min(value = 1, message = "주문 개수는 항상 1보다 커야 합니다.")
	int quantity
) {

}
