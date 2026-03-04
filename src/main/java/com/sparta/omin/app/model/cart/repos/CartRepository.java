package com.sparta.omin.app.model.cart.repos;

import com.sparta.omin.app.model.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci WHERE c.userId = :userId AND c.isDeleted = false")
    Optional<Cart> findByUserIdAndIsDeletedFalseWithItems(@Param("userId") UUID userId);
    Optional<Cart> findByUserIdAndIsDeletedFalse(UUID uuid);
    Optional<Object> findByIdAndUserIdAndIsDeletedFalse(UUID cartId, UUID uuid);
}