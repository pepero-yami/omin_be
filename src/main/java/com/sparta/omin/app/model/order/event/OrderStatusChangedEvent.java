package com.sparta.omin.app.model.order.event;


import com.sparta.omin.app.model.order.entity.status.OrderStatus;

public record OrderStatusChangedEvent(String customerEmail,
                                      OrderStatus status) {
}