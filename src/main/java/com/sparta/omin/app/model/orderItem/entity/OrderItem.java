package com.sparta.omin.app.model.orderItem.entity;

import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.Objects;
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

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price", nullable = false, precision = 10, scale = 7)
    private BigDecimal price;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 7)
    private BigDecimal totalPrice; // 단가 * 수량

    public static OrderItem create(
            Order order,
            UUID productId,
            int quantity,
            BigDecimal price
    ) {
        OrderItem item = new OrderItem();

        item.order = Objects.requireNonNull(order, "order must not be null");
        item.productId = Objects.requireNonNull(productId, "productId must not be null");
        item.quantity = quantity;
        item.price = price;
        item.totalPrice = price.multiply(BigDecimal.valueOf(quantity));
        item.isDeleted = false;

        return item;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
