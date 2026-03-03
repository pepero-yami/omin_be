package com.sparta.omin.app.model.cartItem.entity;

import com.sparta.omin.app.model.cart.entity.Cart;
import com.sparta.omin.app.model.product.entity.Product;
import com.sparta.omin.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "p_cart_item")
public class CartItem extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    public static CartItem create(Cart cart, Product product, int quantity) {
        CartItem cartItem = new CartItem();

        cartItem.cart = cart;
        cartItem.product = product;
        cartItem.quantity = quantity;
        cartItem.createdBy = cart.getUserId();
        cartItem.updatedBy = cart.getUserId();
        cartItem.isDeleted = false;

        return cartItem;
    }

    public void update(int quantity) {
        this.quantity = quantity;
    }
}
