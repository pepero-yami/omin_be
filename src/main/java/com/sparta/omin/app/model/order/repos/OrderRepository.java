package com.sparta.omin.app.model.order.repos;

import com.sparta.omin.app.model.order.entity.Order;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    // N+1(1+1) 방지: Order와 Store를 한 번의 쿼리로 가져옵니다.
    @Query("SELECT o FROM Order o JOIN FETCH o.store WHERE o.id = :orderId")
    Optional<Order> findByIdWithStore(@Param("orderId") UUID orderId);
}
