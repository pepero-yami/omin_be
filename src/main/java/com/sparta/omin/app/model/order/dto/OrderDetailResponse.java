package com.sparta.omin.app.model.order.dto;

import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.entity.status.OrderStatus;
import com.sparta.omin.app.model.orderItem.entity.OrderItem;
import com.sparta.omin.app.model.store.entity.Store;

import java.util.List;
import java.util.UUID;


//0306
public record OrderDetailResponse(
        int status,
        String message,
        OrderDetailsData data
) {
    public static OrderDetailResponse from(Order order) {
        return new OrderDetailResponse(
                200,
                "ok",
                OrderDetailsData.from(
                        order,
                        order.getStore(),
                        order.getOrderItems(),
                        order.getTotalPrice()
//                        order.getStatus()
                )
        );
    }

    public record OrderDetailsData(
            UUID orderId,
            StoreInfo store,
            AddressInfo address,
            OrderStatus orderStatus,
            String userRequest,
            List<OrderItemInfo> orderItems,
            double totalPrice
//            OrderStatus orderStatus
    ) {
        public static OrderDetailsData from(
                Order order,
                Store store,
                List<OrderItem> orderItems,
                double totalPrice
        ) {
            return new OrderDetailsData(
                    order.getId(),
                    new StoreInfo(store.getId(), store.getName()),
                    new AddressInfo(
                            order.getDeliveryAddress(),
                            order.getDeliveryAddressDetail()
                    ),
                    order.getStatus(),
                    order.getUserRequest(),
                    orderItems.stream()
                            .map(OrderItemInfo::from)
                            .toList(),
                    totalPrice
//                    orderStatus
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
