package com.sparta.omin.app.model.cart.service;

import com.sparta.omin.app.model.cart.dto.CartCreateRequest;
import com.sparta.omin.app.model.cart.dto.CartResponse;
import com.sparta.omin.app.model.cart.entity.Cart;
import com.sparta.omin.app.model.cart.repos.CartRepository;
import com.sparta.omin.app.model.cartItem.entity.CartItem;
import com.sparta.omin.app.model.cartItem.repos.CartItemRepository;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public CartResponse addToCart(String userId, CartCreateRequest request) {
        Cart cart = cartRepository.findByUserIdAndIsDeletedFalse(toUuid(userId))
                .orElseGet(() -> cartRepository.save(
                        Cart.create(toUuid(userId), request.storeId())
                ));

        // 같은 가게 상품인지 검증
        if (!cart.getStoreId().equals(request.storeId())) {
            throw new ApiException(ErrorCode.STORE_MISMATCH);
        }

        // 있으면 수량 업데이트, 없으면 insert
        cartItemRepository.findByCartIdAndProductId(cart.getId(), request.productId())
                .ifPresentOrElse(
                        item -> item.update(item.getQuantity() + request.quantity()),
                        () -> cartItemRepository.save(
                                CartItem.create(cart, request.productId(), request.quantity())
                        )
                );

        // 저장 후 cartItems가 채워진 상태로 다시 조회
        Cart refreshedCart = getActiveCart(toUuid(userId));
        return CartResponse.from(refreshedCart);
    }

    public CartResponse getCart(String userId) {
        return cartRepository.findByUserIdAndIsDeletedFalseWithItems(toUuid(userId))
                .map(CartResponse::from)
                .orElse(null);
    }

    //===== helper method =====
    private Cart getActiveCart(UUID userId) {
        return cartRepository.findByUserIdAndIsDeletedFalseWithItems(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.CART_NOT_FOUND));
    }

    private CartItem addCartItem(Cart cart, CartCreateRequest request) {
        // todo productId 임시 주입
        return cartItemRepository.save(CartItem.create(cart, request.productId(), request.quantity()));
    }

    private static UUID toUuid(String userId) {
        return UUID.fromString(userId);
    }
}
