package com.sparta.omin.app.model.cart.service;

import com.sparta.omin.app.model.cart.client.CartRedisClient;
import com.sparta.omin.app.model.cart.entity.RCart;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RCartService {

	private final CartRedisClient redisClient;

	public RCart getCartInfo(UUID userId) {
		RCart cart = redisClient.get(userId, RCart.class);
		return cart != null ? cart : new RCart(userId);
	}

	public RCart getCartInCustomer(UUID userId, UUID storeId) {
		return Optional.ofNullable(redisClient.get(userId, RCart.class))
			.filter(cart -> cart.getStoreId() != null)
			.orElseGet(() -> RCart.create(userId, storeId));
	}

	public RCart save(RCart cart) {
		redisClient.put(cart.getCustomerId(), cart);
		return redisClient.get(cart.getCustomerId(), RCart.class);
	}

	public RCart refresh(UUID userId) {
		redisClient.put(userId, new RCart(userId));
		return redisClient.get(userId, RCart.class);
	}
}
