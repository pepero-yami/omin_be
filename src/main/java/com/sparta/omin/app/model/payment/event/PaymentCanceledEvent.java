package com.sparta.omin.app.model.payment.event;

import java.util.UUID;

public record PaymentCanceledEvent(
        UUID orderId,
        UUID userId
) {}