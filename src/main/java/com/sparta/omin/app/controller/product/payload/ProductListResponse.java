package com.sparta.omin.app.controller.product.payload;

import java.util.List;

public record ProductListResponse(
    List<ProductResponse> products
) {

}
