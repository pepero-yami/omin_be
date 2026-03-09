package com.sparta.omin.app.model.order.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.sparta.omin.app.model.order.entity.status.OrderStatus;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;

import java.util.stream.Stream;

public record OrderStatusRequest(OrderStatus orderStatus) {

    @JsonCreator
    public static OrderStatusRequest create(String requestValue) {
        OrderStatus status = Stream.of(OrderStatus.values())
                .filter(v -> v.name().equalsIgnoreCase(requestValue))
                .findFirst()
                .orElseThrow(() -> new OminBusinessException(ErrorCode.INVALID_ORDER_STATUS));

        return new OrderStatusRequest(status);
    }
}
