package com.sparta.omin.app.model.cartItem.repos;

import com.sparta.omin.app.model.cartItem.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    Optional<CartItem> findByIdAndProductId(UUID cartId, UUID productId);
}
