package com.sparta.omin.app.model.order.repos;

import com.sparta.omin.app.model.order.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Slice<Order> findByIdAndUserIdAndIsDeletedFalse(UUID orderId, UUID userId, Pageable pageable);
    Slice<Order> findByUserIdAndIsDeletedFalse(UUID userId, Pageable pageable);
    Optional<Order> findByIdAndIsDeletedFalse(UUID orderId);

    Slice<Order> findByStoreIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID storeId, Pageable pageable);
}
