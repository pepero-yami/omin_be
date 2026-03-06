package com.sparta.omin.app.model.order.entity;

import com.sparta.omin.app.model.order.entity.status.OrderStatus;
import com.sparta.omin.app.model.orderItem.entity.OrderItem;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.user.entity.User;
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
@Table(name = "p_order")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "user_request", length = 200)
    private String userRequest;

    @Column(name = "delivery_address", length = 100)
    private String deliveryAddress;

    @Column(name = "delivery_address_detail", length = 100)
    private String deliveryAddressDetail;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "total_price")
    private double totalPrice; // 0306

    public static Order create(User user,
                               Store store,
                               String userRequest,
                               String deliveryAddress,
                               String deliveryAddressDetail
    ) {
        Order order = new Order();

        order.user = user;
        order.store = store;
        order.userRequest = userRequest;
        order.deliveryAddress = deliveryAddress;
        order.deliveryAddressDetail = deliveryAddressDetail;
        order.status = OrderStatus.PENDING;
        order.isDeleted = false;

        return order;
    }

    public void delete() {
        this.isDeleted = true;
    }

    //에러방지
    public boolean isCompleted() {
        return false;
    }
}
