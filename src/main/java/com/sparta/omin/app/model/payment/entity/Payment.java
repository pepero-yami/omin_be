package com.sparta.omin.app.model.payment.entity;

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
public class Payment {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "total_price", nullable = false)
    private int totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    public static Payment create(UUID orderId, PaymentMethod paymentMethod, int totalPrice, UUID userId) {
        Payment payment = new Payment();

        payment.orderId = orderId;
        payment.paymentMethod = paymentMethod;
        payment.totalPrice = totalPrice;
        payment.paymentStatus = PaymentStatus.READY;
        payment.createdBy = userId;
        payment.updatedBy = userId;
        payment.isDeleted = false;

        return payment;
    }

    public void delete(UUID userId) {
        this.isDeleted = true;
        this.updatedBy = userId;
    }
}
