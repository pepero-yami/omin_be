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

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "payment_key")
    private String paymentKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "total_price", nullable = false)
    private double totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus paymentStatus;

    // 결제 초기 생성 (READY)
    public static Payment create(UUID orderId, UUID userId, double totalPrice) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.userId = userId;
        payment.totalPrice = totalPrice;
        payment.paymentMethod = PaymentMethod.CREDIT_CARD;
        payment.paymentStatus = PaymentStatus.READY;
        payment.isDeleted = false;
        return payment;
    }

    // 승인 완료 처리
    public void confirm(String paymentKey) {
        this.paymentKey = paymentKey;
        this.paymentStatus = PaymentStatus.SUCCESS;
    }

    // 결제 실패 처리
    public void fail() {
        this.paymentStatus = PaymentStatus.FAILED;
    }

    // 결제 취소 처리
    public void cancel() {
        this.paymentStatus = PaymentStatus.CANCELED;
    }

    public void softDelete() {
        this.isDeleted = true;
    }
}