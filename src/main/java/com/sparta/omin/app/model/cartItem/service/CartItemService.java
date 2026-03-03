package com.sparta.omin.app.model.cartItem.service;

import com.sparta.omin.app.model.cart.entity.Cart;
import com.sparta.omin.app.model.cart.repos.CartRepository;
import com.sparta.omin.app.model.cartItem.dto.CartItemResponse;
import com.sparta.omin.app.model.cartItem.dto.CartItemUpdateRequest;
import com.sparta.omin.app.model.cartItem.entity.CartItem;
import com.sparta.omin.app.model.cartItem.repos.CartItemRepository;
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
    public CartItemResponse create(UUID cartId, CartItemUpdateRequest request) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("조회 가능한 카트가 없습니다."));

        // 같은 가게 상품인지 검증. 일단은 다른 가게일 경우 return
        if (!cart.getStoreId().equals(request.storeId())) {
            throw new IllegalArgumentException("다른 가게의 상품은 담을 수 없습니다.");
        }

        // TODO 만약 같은 가게의 음식을 추후에 추가한다면? insert가 아니라 update가 필요하지않을까?
        getCartItem(cartId, request.productId());

        CartItem cartItem = CartItem.create(cart, request.productId(), request.quantity());
        cartItemRepository.save(cartItem);

        return CartItemResponse.from(cartItem);
    }

    @Transactional
    public CartItemResponse update(UUID cartId, CartItemUpdateRequest request) {
        CartItem cartItem = getCartItem(cartId, request.productId());
        cartItem.update(request.quantity());

        return CartItemResponse.from(cartItem);
    }

    //===== helper method =====
    private CartItem getCartItem(UUID cartId, UUID productId) {
        return cartItemRepository.findByIdAndProductId(cartId, productId)
                .orElseThrow(() -> new IllegalArgumentException("카트가 존재하지 않습니다."));
    }
}
