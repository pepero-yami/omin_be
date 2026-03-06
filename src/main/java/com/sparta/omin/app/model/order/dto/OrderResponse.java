package com.sparta.omin.app.model.order.dto;

import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.entity.status.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        OrderStatus orderStatus,
//        BigDecimal totalPrice,
        LocalDateTime createdAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
//                totalPrice,
                order.getCreatedAt()
        );
    }
}