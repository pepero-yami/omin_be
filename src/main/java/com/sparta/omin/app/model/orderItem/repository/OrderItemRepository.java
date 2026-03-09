package com.sparta.omin.app.model.orderItem.repository;

import com.sparta.omin.app.model.orderItem.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
}
