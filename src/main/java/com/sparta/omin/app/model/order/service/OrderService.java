package com.sparta.omin.app.model.order.service;

import com.sparta.omin.app.model.order.dto.OrderCreateRequest;
import com.sparta.omin.app.model.order.dto.OrderDetailResponse;
import com.sparta.omin.app.model.order.dto.OrderResponse;
import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.repos.OrderRepository;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderResponse createOrder(UUID userId, OrderCreateRequest request) {
        return null;
    }

    public OrderDetailResponse getOrderDetail(UUID orderId) {
        Order order = orderRepository.findByIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_NOT_FOUND));
        return OrderDetailResponse.from(order);
    }

    public Page<OrderResponse> getOrdersHistory(UUID orderId, UUID userId, Pageable pageable) {
        return orderRepository.findByIdAndUserIdAndIsDeletedFalse(orderId, userId, pageable)
                .map(OrderResponse::from);
    }

    public Page<OrderResponse> getOrdersByOwner(UUID storeId, UUID userId, Pageable pageable) {
        //TODO 받아온 storeId와 로그인된 userId가 같은지 검증해야함 - 성원님이랑 상의해볼 것
        return orderRepository.findByUserIdAndIsDeletedFalse(userId, pageable);
    }
}
