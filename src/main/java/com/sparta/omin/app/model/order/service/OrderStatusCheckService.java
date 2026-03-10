package com.sparta.omin.app.model.order.service;

import com.sparta.omin.app.model.order.entity.status.OrderStatus;
import com.sparta.omin.app.model.order.repos.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderStatusCheckService {
    private final OrderRepository orderRepository;

    //진행중인 order status 확인
    public boolean existsProcessingOrder(UUID storeId, Collection<OrderStatus> statuses) {
        return orderRepository.existsByStoreIdAndStatusInAndIsDeletedFalse(storeId, statuses);
    }
}
