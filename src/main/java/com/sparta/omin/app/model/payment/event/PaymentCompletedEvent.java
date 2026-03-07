package com.sparta.omin.app.model.payment.event;

import java.util.UUID;

public record PaymentCompletedEvent(
        UUID orderId,
        UUID userId,
        double amount
) {}