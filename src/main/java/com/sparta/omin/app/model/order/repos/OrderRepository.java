package com.sparta.omin.app.model.order.repos;

import com.sparta.omin.app.model.order.dto.OrderResponse;
import com.sparta.omin.app.model.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Page<Order> findByIdAndUserIdAndIsDeletedFalse(UUID orderId, UUID userId, Pageable pageable);
    Page<OrderResponse> findByUserIdAndIsDeletedFalse(UUID userId, Pageable pageable);
    Optional<Order> findByIdAndIsDeletedFalse(UUID orderId);
}
