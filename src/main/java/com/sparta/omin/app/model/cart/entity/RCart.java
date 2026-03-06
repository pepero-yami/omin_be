package com.sparta.omin.app.model.cart.entity;

import jakarta.persistence.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("Cart")
public class RCart {

	@Id
	private UUID customerId;
	private UUID storeId;
	private Double totalPrice;
	@Builder.Default
	private List<Product> products = new ArrayList<>();

	public RCart(UUID id) {
		this.customerId = id;
	}

	public void calculateTotalPrice() {
		this.totalPrice = products.stream()
			.mapToDouble(Product::getTotalPrice)
			.sum();
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Product {

		private UUID id;
		private String name;
		private Double price;
		private Double totalPrice;
		private int quantity;

		public void add(int quantity) {
			this.quantity += quantity;
			this.totalPrice = this.price * this.quantity;
		}
	}
}

