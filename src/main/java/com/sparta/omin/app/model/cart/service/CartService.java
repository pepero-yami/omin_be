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
    public CartResponse addToCart(UUID userId, CartCreateRequest request, boolean force) {
        Cart cart = getOrCreateCart(userId, request.storeId(), force);
        upsertCartItem(cart, request);

        Cart refreshedCart = getActiveCart(userId);
        return CartResponse.from(refreshedCart);
    }

    public CartResponse getCart(UUID userId) {
        return cartRepository.findByUserIdAndIsDeletedFalseWithItems(userId)
                .map(CartResponse::from)
                .orElse(null);
    }

    @Transactional
    public void deleteCart(UUID userId, UUID cartId) {
        Cart cart = cartRepository.findByIdAndUserIdAndIsDeletedFalse(cartId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.CART_NOT_FOUND));
        cart.delete(userId);
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

    private Cart getOrCreateCart(UUID userId, UUID storeId, boolean force) {
        return cartRepository.findByUserIdAndIsDeletedFalse(userId)
                .map(cart -> {
                    if (!cart.getStoreId().equals(storeId)) {
                        if (!force) throw new ApiException(ErrorCode.CART_STORE_CONFLICT);
                        cart.delete(userId);
                        return cartRepository.save(Cart.create(userId, storeId));
                    }
                    return cart;
                })
                .orElseGet(() -> cartRepository.save(Cart.create(userId, storeId)));
    }

    private void upsertCartItem(Cart cart, CartCreateRequest request) {
        cartItemRepository.findByCartIdAndProductId(cart.getId(), request.productId())
                .ifPresentOrElse(
                        item -> item.update(item.getQuantity() + request.quantity()),
                        () -> cartItemRepository.save(
                                CartItem.create(cart, request.productId(), request.quantity())
                        )
                );
    }
}
