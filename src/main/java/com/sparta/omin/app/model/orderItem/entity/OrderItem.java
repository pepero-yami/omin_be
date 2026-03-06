package com.sparta.omin.app.model.orderItem.entity;

import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.product.entity.Product;
import com.sparta.omin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "p_order_item")
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price", nullable = false, precision = 10, scale = 7)
    private double price;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 7)
    private double totalPrice; // 단가 * 수량

    public static OrderItem create(Order order, Product product, int quantity, double price) {
        OrderItem orderItem = new OrderItem();

        orderItem.order = order;
        orderItem.product = product;
        orderItem.quantity = quantity;
        orderItem.price = price;
        orderItem.totalPrice = quantity * price;
        orderItem.isDeleted = false;

        return orderItem;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
