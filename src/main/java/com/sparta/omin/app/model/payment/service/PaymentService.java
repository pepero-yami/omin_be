package com.sparta.omin.app.model.payment.service;

import com.sparta.omin.app.model.payment.dto.PaymentResponse;
import com.sparta.omin.app.model.payment.entity.Payment;
import com.sparta.omin.app.model.payment.entity.PaymentStatus;
import com.sparta.omin.app.model.payment.event.PaymentCompletedEvent;
import com.sparta.omin.app.model.payment.repos.PaymentRepository;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID orderId, UUID userId) {
        Payment payment = paymentRepository.findByOrderIdAndUserIdAndIsDeletedFalse(orderId, userId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        return PaymentResponse.from(payment);
    }

    // 1. 결제 요청 (READY 상태 생성)
    @Transactional
    public PaymentResponse requestPayment(UUID orderId, UUID userId, double amount) {
        // 이미 진행 중인 결제가 있는지 확인
        paymentRepository.findByOrderIdAndIsDeletedFalse(orderId)
                .ifPresent(p -> {
                    if(p.getPaymentStatus() == PaymentStatus.SUCCESS) {
                        throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR, "이미 결제가 완료된 주문입니다.");
                    }
                });

        Payment payment = Payment.create(orderId, userId, amount);
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    // 2. 결제 승인 (Toss 결제 완료 후 호출됨)
    @Transactional
    public PaymentResponse confirmPayment(UUID orderId, String paymentKey, double amount) {
        Payment payment = paymentRepository.findByOrderIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new ApiException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getPaymentStatus() != PaymentStatus.READY) {
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR, "결제 대기 상태가 아닙니다."); //FIXME 에러코드 추가하기
        }

        // 실제 연동은 안 하지만 금액 검증은 일단 넣음
        if (payment.getTotalPrice() != amount) {
            payment.fail();
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR, "결제 금액이 일치하지 않습니다.");
        }

        payment.confirm(paymentKey);

        // 이벤트 발행 (MSA 대비 - 주문 서비스에서 이 이벤트를 구독하여 상태 변경) - 이벤트 기반 비동기 지향!
        // 결제 서비스가 주문 서비스를 직접 호출하지 않고 PaymentCompletedEvent를 던짐 -> 나중에 MSA로 전환할 때 코드 수정 없이 메시지 브큐(Kafka 등)만 연결하면 되는 구조라고 합니다
        eventPublisher.publishEvent(new PaymentCompletedEvent(orderId, payment.getUserId(), amount));

        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse cancelPayment(UUID paymentId, UUID userId) {
        Payment payment = paymentRepository.findByIdAndIsDeletedFalse(paymentId)
                .orElseThrow(() -> new ApiException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.PAYMENT_UNAUTHORIZED);
        }

        if (payment.getPaymentStatus() == PaymentStatus.CANCELED) {
            throw new ApiException(ErrorCode.PAYMENT_ALREADY_CANCELED);
        }

        payment.cancel();

        // TODO: 결제 취소 시 주문 상태도 같이 취소되는 로직 (주문 상태 업데이트 이벤트 발행 등) - 필요할까?
        // eventPublisher.publishEvent(new PaymentCanceledEvent(payment.getOrderId()));

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPayments(UUID customerId, Pageable pageable) {
        return paymentRepository.findByUserIdAndIsDeletedFalse(customerId, pageable)
                .map(PaymentResponse::from);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByAdmin(UUID paymentId) {
        Payment payment = paymentRepository.findByIdAndIsDeletedFalse(paymentId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        return PaymentResponse.from(payment);
    }
}