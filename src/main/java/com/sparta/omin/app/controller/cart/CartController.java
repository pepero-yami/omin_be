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

import java.security.Principal;
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
    public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody CartCreateRequest request,
                                                  @RequestParam(defaultValue = "false") boolean force,
                                                  Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addToCart(principal.getName(), request, force));
    }

    @GetMapping("/cart")
    public ResponseEntity<CartResponse> getCart(Principal principal) {
        return ResponseEntity.ok(cartService.getCart(principal.getName()));
    }

    // TODO (create 메서드와 통합시켜야함 - 완료)  유지사유 : API 명세서와의 통일성 위해 일단 유지
    @PostMapping("/cart/{cartId}")
    public ResponseEntity<CartItemResponse> addCartItem(@PathVariable UUID cartId, @RequestBody CartItemUpdateRequest request, Principal principal) {
        return ResponseEntity.ok(cartItemService.create(principal.getName(), cartId, request));
    }

    @PatchMapping("/cart/{cartId}")
    public ResponseEntity<CartItemResponse> updateQuantity(@PathVariable UUID cartId, @RequestBody CartItemUpdateRequest request, Principal principal) {
        return ResponseEntity.ok(cartItemService.updateQuantity(principal.getName(), cartId, request));
    }

    @DeleteMapping("/cart/{cartId}")
    public ResponseEntity<Void> deleteCart(Principal principal) {
        cartService.deleteCart(principal.getName());
        return ResponseEntity.noContent().build();
    }
}
