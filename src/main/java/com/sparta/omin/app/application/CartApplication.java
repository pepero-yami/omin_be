package com.sparta.omin.app.application;

import com.sparta.omin.app.model.cart.dto.CartAddProductRequest;
import com.sparta.omin.app.model.cart.entity.RCart;
import com.sparta.omin.app.model.cart.service.RCartService;
import com.sparta.omin.app.model.product.entity.Product;
import com.sparta.omin.app.model.product.service.ProductReadService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartApplication {
	private final RCartService cartService;
	private final ProductReadService productReadService;

	public RCart addCart(UUID customerId, CartAddProductRequest request) {
		Product product = productReadService.getProductById(request.productId());
	}
}
