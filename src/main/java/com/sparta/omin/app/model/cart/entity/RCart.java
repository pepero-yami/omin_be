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
	private List<CartItem> products = new ArrayList<>();

	public RCart(UUID id) {
		this.customerId = id;
	}

	public void calculateTotalPrice() {
		this.totalPrice = products.stream()
			.mapToDouble(CartItem::getTotalPrice)
			.sum();
	}

	public static RCart create(UUID customerId, UUID storeId) {
		return RCart.builder()
			.customerId(customerId)
			.storeId(storeId)
			.build();
	}

	public static void addNewCartItem(RCart cart, CartItem cartItem) {
		cart.getProducts().add(cartItem);
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class CartItem {

		private UUID id;
		private String name;
		private Double price;
		private Double totalPrice;
		private int quantity;

		public void addQuantity(int quantity) {
			this.quantity += quantity;
			this.totalPrice = this.price * this.quantity;
		}
	}
}

