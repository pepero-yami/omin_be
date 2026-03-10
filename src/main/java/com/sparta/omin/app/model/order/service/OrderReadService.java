package com.sparta.omin.app.model.order.service;

import com.sparta.omin.app.model.order.dto.OrderInternalDto;
import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.repos.OrderRepository;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderReadService {

    private final OrderRepository orderRepository;

    public Order getOrder(UUID orderId) {
        return orderRepository.findByIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    public OrderInternalDto getOrderForPayment(UUID orderId) {
        Order order = getOrder(orderId);
        return new OrderInternalDto(
                order.getId(),
                order.getUser().getId(),
                order.getTotalPrice()
        );
    }
}
