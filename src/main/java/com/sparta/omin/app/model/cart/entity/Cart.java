package com.sparta.omin.app.model.cart.entity;

import com.sparta.omin.app.model.cartItem.entity.CartItem;
import com.sparta.omin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "p_cart")
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false)
    private UUID id;
//
//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    private User user;

//    @ManyToOne
//    @JoinColumn(name = "store_id")
//    private Store store

    // TODO 연관관계설정에 있어서 고민해보기
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CartItem> cartItems = new ArrayList<>();

    @Column(name = "total_price", nullable = false)
    private int totalPrice;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    public static Cart create(UUID userId, UUID storeId) {
        Cart cart = new Cart();

        cart.userId = userId;
        cart.storeId = storeId;
        cart.createdBy = userId;
        cart.updatedBy = userId;
        cart.isDeleted = false;

        return cart;
    }

    public void delete(UUID userId) {
        this.isDeleted = true;
        this.updatedBy = userId;
    }

    public void addItem(CartItem item) {
        this.cartItems.add(item);
    }
}