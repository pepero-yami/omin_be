package com.sparta.omin.app.controller.cart;

import com.sparta.omin.app.model.cart.dto.CartCreateRequest;
import com.sparta.omin.app.model.cart.dto.CartResponse;
import com.sparta.omin.app.model.cart.service.CartService;
import com.sparta.omin.app.model.cartItem.dto.CartItemResponse;
import com.sparta.omin.app.model.cartItem.dto.CartItemUpdateRequest;
import com.sparta.omin.app.model.cartItem.service.CartItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CartController {

    private final CartService cartService;
    private final CartItemService cartItemService;

    /**
     * 첫 상품 담기 → 카트 없으면 카트 생성 + 카트 아이템 추가
     * 두 번째 상품 담기 → 기존 카트에 카트 아이템만 추가
     * 주문 완료 → 카트 삭제 (or 비우기)
     */
    @PostMapping("/cart")
    public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody CartCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addToCart(request));
    }

    /**
     * TODO 인증 구현시 변경
     */
    @GetMapping("/cart/{userId}")
    public ResponseEntity<CartResponse> get(@PathVariable UUID userId) {
        return ResponseEntity.ok(cartService.get(userId));
    }

    // TODO create 메서드와 통합시켜야함
    @PostMapping("/cart/{cartId}")
    public ResponseEntity<CartItemResponse> addCartItem(@PathVariable UUID cartId, @RequestBody CartItemUpdateRequest request) {
        return ResponseEntity.ok(cartItemService.create(cartId, request));
    }

    @PatchMapping("/cart/{cartId}")
    public ResponseEntity<CartItemResponse> updateCartItem(@PathVariable UUID cartId, @RequestBody CartItemUpdateRequest request) {
        return ResponseEntity.ok(cartItemService.update(cartId, request));
    }
}
