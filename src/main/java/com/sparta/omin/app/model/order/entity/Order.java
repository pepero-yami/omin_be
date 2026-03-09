package com.sparta.omin.app.model.order.entity;

import com.sparta.omin.app.model.address.entity.Address;
import com.sparta.omin.app.model.order.entity.status.OrderStatus;
import com.sparta.omin.app.model.orderItem.entity.OrderItem;
import com.sparta.omin.app.model.product.entity.Product;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.common.entity.BaseEntity;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Column(name = "delivery_address", length = 200)
    private String deliveryAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "total_price")
    private double totalPrice;

    public static Order create(User user,
                               Store store,
                               String userRequest,
                               Address address
    ) {
        Order order = new Order();

        order.user = user;
        order.store = store;
        order.userRequest = userRequest;
        order.deliveryAddress = address.getRoadAddress() + " " + address.getShippingDetailAddress();
        order.orderItems = new ArrayList<>();
        order.status = OrderStatus.PENDING;
        order.isDeleted = false;

        return order;
    }

    public void update(Address address, String userRequest) {
        validatePendingStatus();

        if (address != null) {
            this.deliveryAddress = address.getRoadAddress() + " " + address.getShippingDetailAddress();
        }

        if (userRequest != null) {
            this.userRequest = userRequest;
        }
    }

    public void cancel() {
        validatePendingStatus();

        if (this.createdAt.plusMinutes(5).isBefore(LocalDateTime.now())) {
            throw new OminBusinessException(ErrorCode.ORDER_PERIOD_EXPIRED);
        }

        this.status = OrderStatus.CANCELLED;
    }

    public void addOrderItems(List<Product> products, Map<UUID, Integer> quantityMap) {
        products.forEach(product ->
                this.orderItems.add(OrderItem.create(this, product, quantityMap.get(product.getId())))
        );

        // 주문 총액 다시 계산
        this.totalPrice = this.orderItems.stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
    }

    //에러방지
    public boolean isCompleted() {
        return false;
    }

    //validation
    private void validatePendingStatus() {
        if (this.getStatus() != OrderStatus.PENDING) {
            throw new OminBusinessException(ErrorCode.ORDER_UPDATE_DENIED);
        }
    }
}
