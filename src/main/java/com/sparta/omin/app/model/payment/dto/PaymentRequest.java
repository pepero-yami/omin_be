package com.sparta.omin.app.model.payment.dto;

import java.util.UUID;

public record PaymentRequest(UUID orderId, double amount) {
}