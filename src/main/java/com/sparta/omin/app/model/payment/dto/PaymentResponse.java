package com.sparta.omin.app.model.payment.dto;

import com.sparta.omin.app.model.payment.entity.Payment;
import com.sparta.omin.app.model.payment.entity.PaymentMethod;
import com.sparta.omin.app.model.payment.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        UUID orderId,
        PaymentMethod paymentMethod,
        double totalPrice,
        LocalDateTime createdAt,
        PaymentStatus paymentStatus,
        String paymentKey
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getPaymentMethod(),
                payment.getTotalPrice(),
                payment.getCreatedAt(),
                payment.getPaymentStatus(),
                payment.getPaymentKey()
        );
    }
}