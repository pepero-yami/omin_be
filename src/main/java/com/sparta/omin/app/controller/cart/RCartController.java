package com.sparta.omin.app.controller.cart;

import com.sparta.omin.app.application.CartApplication;
import com.sparta.omin.app.model.cart.dto.CartAddProductRequest;
import com.sparta.omin.app.model.cart.dto.request.CartProductDeleteRequest;
import com.sparta.omin.app.model.cart.entity.RCart;
import com.sparta.omin.app.model.cart.service.RCartService;
import com.sparta.omin.app.model.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/carts")
public class RCartController {

	private final CartApplication cartApplication;
	private final RCartService cartService;

	@PostMapping
	public ResponseEntity<RCart> addCartProduct(
		@AuthenticationPrincipal User user,
		@Valid @RequestBody CartAddProductRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(cartApplication.addCart(user.getId(), request));
	}

	@GetMapping
	public ResponseEntity<RCart> getCartInfo(@AuthenticationPrincipal User user) {
		return ResponseEntity.ok().body(cartService.getCartInfo(user.getId()));
	}

	@PutMapping
	public ResponseEntity<RCart> refreshCart(@AuthenticationPrincipal User user) {
		return ResponseEntity.status(HttpStatus.CREATED).body(
			cartService.refresh(user.getId())
		);
	}

	@PostMapping("/products")
	public ResponseEntity<RCart> deleteProductInCart(
		@AuthenticationPrincipal User user,
		@Valid @RequestBody CartProductDeleteRequest request
	) {
		return ResponseEntity.ok().body(
			cartApplication.deleteProductInCart(user.getId(), request)
		);
	}

	@DeleteMapping
	public ResponseEntity<Void> cartDelete(
		@AuthenticationPrincipal User user
	) {
		cartService.delete(user.getId());
		return ResponseEntity.noContent().build();
	}
}
