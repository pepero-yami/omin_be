package com.sparta.omin.app.model.payment.entity;

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
@Table(name = "p_payment")
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /**
     * TODO 주문생성 - 결제실패 - 재결제시도 - 결제 성공시에는 (???) OneToMany가 맞을지도?!
     * 현재 요구사항정의서는 상태코드를 바꾸기로 되어있는데, 그러면 실패이력은 안 남겨도되는지?
     */

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "order_id", nullable = false)
//    private Order order;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "total_price", nullable = false)
    private int totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus paymentStatus;


    public static Payment create(UUID orderId, PaymentMethod paymentMethod, int totalPrice, UUID userId) {
        Payment payment = new Payment();

        payment.orderId = orderId;
        payment.paymentMethod = paymentMethod;
        payment.totalPrice = totalPrice;
        payment.paymentStatus = PaymentStatus.READY;
        payment.isDeleted = false;

        return payment;
    }

    public void softDelete() {
        this.isDeleted = true;
    }
}