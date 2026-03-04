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
    public CartItemResponse create(UUID userId, UUID cartId, CartItemUpdateRequest request) {
        Cart cart = getActiveCart(userId);

        // 같은 가게 상품인지 검증. 일단은 다른 가게일 경우 return
        if (!cart.getStoreId().equals(request.storeId())) {
            throw new ApiException(ErrorCode.STORE_MISMATCH);
        }

        // 새 상품이면 insert, 이미 있다면 수량 추가 후 update
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.productId())
                .map(item -> {
                    item.update(item.getQuantity() + request.quantity());
                    return cartItemRepository.save(item);
                })
                .orElseGet(() -> cartItemRepository.save(
                        CartItem.create(cart, request.productId(), request.quantity())
                ));

        return CartItemResponse.from(cartItem);
    }

    @Transactional
    public CartItemResponse updateQuantity(UUID userId, UUID cartId, CartItemUpdateRequest request) {
        Cart cart = getActiveCart(userId);
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
