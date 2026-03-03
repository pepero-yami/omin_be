package com.sparta.omin.app.model.cart.service;

import com.sparta.omin.app.model.cart.dto.CartCreateRequest;
import com.sparta.omin.app.model.cart.dto.CartResponse;
import com.sparta.omin.app.model.cart.entity.Cart;
import com.sparta.omin.app.model.cart.repos.CartRepository;
import com.sparta.omin.app.model.cartItem.entity.CartItem;
import com.sparta.omin.app.model.cartItem.repos.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public CartResponse addToCart(CartCreateRequest request) {
        Optional<Cart> existingCart = cartRepository.findByUserIdAndIsDeletedFalse(request.userId());
        Cart cart;

        if (existingCart.isPresent()) {
            cart = existingCart.get();
            addCartItem(cart, request);
        } else {
            cart = Cart.create(request.userId(), request.storeId());
            cartRepository.save(cart);

            CartItem cartItem = addCartItem(cart, request);
            cartItemRepository.save(cartItem);
        }
        return CartResponse.from(cart);
    }

    public CartResponse get(UUID userId) {
        Cart cart = getActiveCart(userId);
        return CartResponse.from(cart);
    }


    //===== helper method =====
    private Cart getActiveCart(UUID userId) {
        return cartRepository.findByUserIdAndIsDeletedFalseWithItems(userId)
                .orElseThrow(() -> new IllegalArgumentException("조회 가능한 카트가 없습니다."));
    }

    private CartItem addCartItem(Cart cart, CartCreateRequest request) {
        return cartItemRepository.save(CartItem.create(cart, request.productId(), request.quantity()));
    }
}
