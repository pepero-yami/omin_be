package com.sparta.omin.app.model.cart.repos;

import com.sparta.omin.app.model.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
}
