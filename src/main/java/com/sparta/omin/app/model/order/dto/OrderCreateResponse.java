package com.sparta.omin.app.model.order.dto;

import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.entity.status.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCreateResponse(
        UUID orderId,
        OrderStatus orderStatus,
        Double totalPrice,
        LocalDateTime createdAt
) {
    public static OrderCreateResponse from(Order order) {
        return new OrderCreateResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getCreatedAt()
        );
    }
}