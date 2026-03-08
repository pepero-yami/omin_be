package com.sparta.omin.app.model.order.repos;

import com.sparta.omin.app.model.order.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.store " +
            "WHERE o.user.id = :userId AND o.isDeleted = false " +
            "AND o.store.isDeleted = false " +
            "ORDER BY o.createdAt DESC")
    Slice<Order> findByUserIdWithStore(@Param("userId") UUID userId, Pageable pageable);
    Slice<Order> findByUserIdAndIsDeletedFalse(UUID userId, Pageable pageable);
    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.store " +
            "LEFT JOIN FETCH o.orderItems " +
            "WHERE o.id = :orderId AND o.isDeleted = false")
    Optional<Order> findByIdAndIsDeletedFalse(UUID orderId);
    Slice<Order> findByStoreIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID storeId, Pageable pageable);
}
