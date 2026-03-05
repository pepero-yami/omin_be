package com.sparta.omin.app.controller.cart;

import com.sparta.omin.app.application.CartApplication;
import com.sparta.omin.app.model.cart.dto.CartAddProductRequest;
import com.sparta.omin.app.model.cart.entity.RCart;
import com.sparta.omin.app.model.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
public class RCartController {

	private final CartApplication cartApplication;

	@PostMapping
	public ResponseEntity<RCart> addCart(
		@AuthenticationPrincipal User user,
		@RequestBody CartAddProductRequest request) {
		return ResponseEntity.ok(cartApplication.addCart(user.getId(), request));
	}
}
