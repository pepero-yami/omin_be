package com.sparta.omin.app.model.cart.entity;

import jakarta.persistence.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@Data
@NoArgsConstructor
@RedisHash("Cart")
public class RCart {

	@Id
	private UUID customerId;
	private UUID storeId;
	private int totalPrice;
	private List<Product> products = new ArrayList<>();
	private List<String> messages = new ArrayList<>();

	public RCart(UUID id) {
		this.customerId = id;
	}

	public void addMessage(String message) {
		this.messages.add(message);
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Product {
		private UUID productId;
		private String name;
		private int quantity;
	}
}
