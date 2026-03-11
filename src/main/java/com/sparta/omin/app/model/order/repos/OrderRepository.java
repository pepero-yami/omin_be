package com.sparta.omin.app.model.order.repos;

import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.entity.status.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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
    @Query("SELECT o FROM Order o WHERE o.store.id = :storeId " +
            "AND o.isDeleted = false " +
            "AND (:status IS NULL OR o.status = :status)")
    Slice<Order> findByStoreIdWithStatus(@Param("storeId") UUID storeId,
                                         @Param("status") OrderStatus status,
                                         Pageable pageable);
    boolean existsByStoreIdAndStatusInAndIsDeletedFalse(UUID storeId, Collection<OrderStatus> statuses);
}
