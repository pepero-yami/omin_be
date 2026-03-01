package com.sparta.omin.app.controller.cart;

import com.sparta.omin.app.model.cart.dto.CartCreateRequest;
import com.sparta.omin.app.model.cart.dto.CartResponse;
import com.sparta.omin.app.model.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CartController {

    private final CartService cartService;

    @PostMapping("/cart")
    public ResponseEntity<CartResponse> create(@Valid @RequestBody CartCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.create(request));
    }
}
