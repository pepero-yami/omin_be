package com.sparta.omin.app.model.order.dto;

import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.entity.status.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        OrderStatus orderStatus,
        String userRequest,
        String storeName,
        double totalPrice,
        LocalDateTime createdAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getUserRequest(),
                order.getStore().getName(),
                order.getTotalPrice(),
                order.getCreatedAt()
        );
    }
}