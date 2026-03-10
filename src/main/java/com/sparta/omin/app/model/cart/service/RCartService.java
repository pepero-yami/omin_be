package com.sparta.omin.app.model.cart.service;

import com.sparta.omin.app.model.cart.client.CartRedisClient;
import com.sparta.omin.app.model.cart.entity.RCart;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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

	@Transactional
	public RCart save(RCart cart) {
		redisClient.put(cart.getCustomerId(), cart);
		return getCartInfo(cart.getCustomerId());
	}

	@Transactional
	public RCart refresh(UUID userId) {
		redisClient.put(userId, RCart.create(userId, null));
		return getCartInfo(userId);
	}

	@Transactional
	public void delete(UUID userId) {
		redisClient.delete(userId);
	}
}
