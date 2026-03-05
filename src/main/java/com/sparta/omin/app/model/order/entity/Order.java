package com.sparta.omin.app.model.order.entity;

import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "p_order")
@Getter

@NoArgsConstructor
// 임시 Stub입니다. 이 파일을 무시해주세요.
public class Order extends BaseEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false, updatable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // forTest
    public static Order createWithId(User user, Store store) {
        Order order = new Order();
        order.user = user;
        order.store = store;
        order.status = OrderStatus.COMPLETED;
        return order;
    }

    public boolean isCompleted() {
        return this.status == OrderStatus.COMPLETED;
    }


    public enum OrderStatus {
        COMPLETED
    }
}