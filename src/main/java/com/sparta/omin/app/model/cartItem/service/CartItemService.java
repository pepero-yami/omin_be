package com.sparta.omin.app.model.cartItem.service;

import com.sparta.omin.app.model.cart.entity.Cart;
import com.sparta.omin.app.model.cart.repos.CartRepository;
import com.sparta.omin.app.model.cartItem.dto.CartItemResponse;
import com.sparta.omin.app.model.cartItem.dto.CartItemUpdateRequest;
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
public class CartItemService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public CartItemResponse updateQuantity(UUID userId, UUID cartId, CartItemUpdateRequest request) {
        Cart cart = getActiveCart(userId);

        if (!cart.getId().equals(cartId)) {
            throw new ApiException(ErrorCode.CART_NOT_FOUND);
        }

        CartItem cartItem = getCartItem(cart.getId(), request.productId());
        cartItem.update(request.quantity());
        return CartItemResponse.from(cartItem);
    }

    //===== helper method =====
    private Cart getActiveCart(UUID userId) {
        return cartRepository.findByUserIdAndIsDeletedFalseWithItems(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.CART_NOT_FOUND));
    }

    private CartItem getCartItem(UUID cartId, UUID productId) {
        return cartItemRepository.findByCartIdAndProductId(cartId, productId)
                .orElseThrow(() -> new ApiException(ErrorCode.CART_ITEM_NOT_FOUND));
    }
}
