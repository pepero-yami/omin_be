package com.sparta.omin.app.model.order.dto;

import com.sparta.omin.app.model.address.entity.Address;
import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.entity.status.OrderStatus;
import com.sparta.omin.app.model.orderItem.entity.OrderItem;
import com.sparta.omin.app.model.store.entity.Store;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponse(
        int status,
        String message,
        OrderDetailsData data
) {
    public record OrderDetailsData(
            UUID orderId,
            StoreInfo store,
            AddressInfo address,
            OrderStatus orderStatus,
            String userRequest,
            List<OrderItemInfo> orderItems,
            BigDecimal totalPrice,
            String paymentStatus
    ) {
        public static OrderDetailsData from(
                Order order,
                Store store,
                Address address,
                List<OrderItem> orderItems,
                BigDecimal totalPrice,
                String paymentStatus
        ) {
            return new OrderDetailsData(
                    order.getId(),
                    new StoreInfo(store.getId(), store.getName()),
                    new AddressInfo(address.getRoadAddress(), address.getShippingDetailAddress()),
                    order.getStatus(),
                    order.getUserRequest(),
                    orderItems.stream()
                            .map(OrderItemInfo::from)
                            .toList(),
                    totalPrice,
                    paymentStatus
            );
        }
    }

    public record StoreInfo(
            UUID storeId,
            String storeName
    ) { }

    public record AddressInfo(
            String roadAddress,
            String shippingDetailAddress
    ) { }

    public record OrderItemInfo(
            String productName,
            int quantity,
            BigDecimal itemPrice,
            BigDecimal totalPrice
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
