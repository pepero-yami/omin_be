package com.sparta.omin.app.model.order.dto;

import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.entity.status.OrderStatus;
import com.sparta.omin.app.model.orderItem.entity.OrderItem;

import java.util.List;
import java.util.UUID;

/**
 *
 */
public record OrderDetailResponse(
        UUID orderId,
        StoreInfo store,
        String deliveryAddress,
        OrderStatus orderStatus,
        String userRequest,
        List<OrderItemInfo> orderItems,
        double totalPrice
) {
    public static OrderDetailResponse from(Order order) {
        return new OrderDetailResponse(
                order.getId(),
                new StoreInfo(order.getStore().getId(), order.getStore().getName()),
                order.getDeliveryAddress(),
                order.getStatus(),
                order.getUserRequest(),
                order.getOrderItems().stream()
                        .map(OrderItemInfo::from)
                        .toList(),
                order.getTotalPrice()
        );
    }

    public record StoreInfo(
            UUID storeId,
            String storeName
    ) { }

    public record OrderItemInfo(
            String productName,
            int quantity,
            double itemPrice,
            double totalPrice
    ) {
        public static OrderItemInfo from(OrderItem orderItem) {
            return new OrderItemInfo(
                    orderItem.getProduct().getName(),
                    orderItem.getQuantity(),
                    orderItem.getPrice(),
                    orderItem.getTotalPrice()
            );
        }
    }
}