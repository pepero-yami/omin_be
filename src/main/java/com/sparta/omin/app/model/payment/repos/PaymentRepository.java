package com.sparta.omin.app.model.payment.repos;

import com.sparta.omin.app.model.payment.entity.Payment;
import com.sparta.omin.app.model.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderIdAndIsDeletedFalse(UUID orderId);
    Optional<Payment> findByOrderIdAndUserIdAndIsDeletedFalse(UUID orderId, UUID userId);
    Page<Payment> findByUserIdAndIsDeletedFalse(UUID userId, Pageable pageable);
    Page<Payment> findByUserIdAndPaymentStatusAndIsDeletedFalse(UUID userId, PaymentStatus status, Pageable pageable);    // 결제 상태 검색 조건 추가
    Optional<Payment> findByIdAndIsDeletedFalse(UUID paymentId);
}