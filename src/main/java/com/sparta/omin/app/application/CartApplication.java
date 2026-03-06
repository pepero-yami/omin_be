package com.sparta.omin.app.application;

import com.sparta.omin.app.model.cart.dto.CartAddProductRequest;
import com.sparta.omin.app.model.cart.entity.RCart;
import com.sparta.omin.app.model.cart.service.RCartService;
import com.sparta.omin.app.model.product.service.ProductReadService;
import com.sparta.omin.app.model.product.code.ProductStatus;
import com.sparta.omin.app.model.product.entity.Product;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.constants.ErrorCode;
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
		Product product = productReadService.getProductInStore(request.productId(), request.storeId());
		validateProductStatus(product);

		RCart cart = cartService.getCartInCustomer(customerId, request.storeId());
		validateCartStore(cart, request.storeId());

		cart.getProducts().stream()
			.filter(p -> p.getId().equals(request.productId()))
			.findFirst()
			.ifPresentOrElse(
				existingProduct -> existingProduct.add(request.quantity()),
				() -> addNewProduct(cart, product, request.quantity())
			);
		cart.calculateTotalPrice();
		return cartService.save(cart);
	}

	private void validateProductStatus(Product product) {
		if (product.getStatus() != ProductStatus.ON_SALE) {
			throw new ApiException(ErrorCode.PRODUCT_IS_NOT_AVAILABLE_FOR_SALE);
		}
	}

	private void validateCartStore(RCart cart, UUID requestStoreId) {
		if (!cart.getStoreId().equals(requestStoreId)) {
			throw new ApiException(ErrorCode.CART_STORE_CONFLICT);
		}
	}

	private void addNewProduct(RCart cart, Product product, int quantity) {
		cart.getProducts().add(RCart.Product.builder()
			.id(product.getId())
			.name(product.getName())
			.price(product.getPrice())
			.quantity(quantity)
			.totalPrice(product.getPrice() * quantity)
			.build());
	}
}