package com.sparta.omin.app.model.payment.dto;

import java.util.UUID;

public record PaymentConfirmRequest(UUID orderId, String paymentKey, double amount) {}