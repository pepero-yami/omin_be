package com.sparta.omin.app.model.cart.service;

import com.sparta.omin.app.model.cart.client.CartRedisClient;
import com.sparta.omin.app.model.cart.entity.RCart;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RCartService {

	private final CartRedisClient redisClient;

	public RCart getCartInfo(UUID userId) {
		return redisClient.get(userId)
			.orElseGet(()-> RCart.create(userId, null));
	}

	public RCart getCartInCustomer(UUID userId, UUID storeId) {
		return redisClient.get(userId)
			.filter(cart -> cart.getStoreId() != null)
			.orElseGet(() -> RCart.create(userId, storeId));
	}

	public RCart save(RCart cart) {
		redisClient.put(cart.getCustomerId(), cart);
		return getCartInfo(cart.getCustomerId());
	}

	public RCart refresh(UUID userId) {
		redisClient.put(userId, new RCart(userId));
		return getCartInfo(userId);
	}
}
