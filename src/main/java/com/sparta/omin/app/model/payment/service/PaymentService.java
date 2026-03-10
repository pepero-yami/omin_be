package com.sparta.omin.app.model.payment.service;

import com.sparta.omin.app.model.order.dto.OrderInternalDto;
import com.sparta.omin.app.model.order.service.OrderReadService;
import com.sparta.omin.app.model.payment.dto.PaymentResponse;
import com.sparta.omin.app.model.payment.entity.Payment;
import com.sparta.omin.app.model.payment.entity.PaymentStatus;
import com.sparta.omin.app.model.payment.event.PaymentCanceledEvent;
import com.sparta.omin.app.model.payment.event.PaymentCompletedEvent;
import com.sparta.omin.app.model.payment.repos.PaymentRepository;
import com.sparta.omin.app.model.user.service.UserReadService;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderReadService orderReadService;
    private final UserReadService userReadService;
    private final ApplicationEventPublisher eventPublisher;

    // 1. 결제 요청 (READY 상태 생성)
    @Transactional
    public PaymentResponse requestPayment(UUID orderId, UUID userId, double amount) {

        // 1) 주문 정보 및 소유권 확인 - 엔티티 대신 DTO를 받아옴(MSA)
        OrderInternalDto order = orderReadService.getOrderForPayment(orderId);

        // 본인 주문 확인 (Dto의 정보를 활용)
        if (!order.userId().equals(userId)) {
            throw new OminBusinessException(ErrorCode.PAYMENT_ORDER_USER_MISMATCH);
        }

        // 금액 검증 - 주문 서비스의 실제 금액과 요청 금액이 일치하는지 확인
        if (order.totalPrice() != amount) {
            throw new OminBusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 2) 중복 결제 요청 확인 (멱등성)
        var existingPayment = paymentRepository.findByOrderIdAndIsDeletedFalse(orderId);

        if (existingPayment.isPresent()) {
            Payment p = existingPayment.get();

            // 이미 성공한 경우 -> 에러
            if (p.getPaymentStatus() == PaymentStatus.SUCCESS) {
                throw new OminBusinessException(ErrorCode.PAYMENT_ALREADY_COMPLETED);
            }
            // 이미 취소된 주문인 경우 -> 에러 (새로 주문하도록 유도)
            if (p.getPaymentStatus() == PaymentStatus.CANCELED) {
                throw new OminBusinessException(ErrorCode.PAYMENT_ORDER_ALREADY_CANCELED);
            }
            // 이미 READY 상태라면 새로 저장하지 않고 기존 것을 그대로 반환
            if (p.getPaymentStatus() == PaymentStatus.READY) {
                return PaymentResponse.from(p);
            }
        }

        // 기존 내역이 없거나 특이사항이 없으면 새로 생성
        Payment payment = Payment.create(orderId, userId, amount);
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    // 2. 결제 승인 (Toss등 외부에서 결제 완료 후 호출됨) 멱등성 보장
    @Transactional
    public PaymentResponse confirmPayment(UUID orderId, String paymentKey, double amount, UUID userId) {
        Payment payment = paymentRepository.findByOrderIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 이미 성공했다면 그대로 반환 (멱등성)
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return PaymentResponse.from(payment);
        }

        // 1) 결제 정보 소유권(권한) 확인
        if (!payment.getUserId().equals(userId)) {
            throw new OminBusinessException(ErrorCode.PAYMENT_UNAUTHORIZED);
        }

        // 2) 결제 가능 상태 확인 (READY가 아닌 경우)
        if (payment.getPaymentStatus() != PaymentStatus.READY) {
            throw new OminBusinessException(ErrorCode.PAYMENT_INVALID_STATUS);
        }

        // 3) 금액 검증 (토스에서 넘어온 금액과 DB의 주문 금액 대조)
        if (payment.getTotalPrice() != amount) {
            payment.fail();
            throw new OminBusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 승인 처리
        payment.confirm(paymentKey);

        // 이벤트 발행 (OrderEventListener가 이를 받아 주문 상태를 변경함)
        // 결제 서비스가 주문 서비스를 직접 호출하지 않고 PaymentCompletedEvent를 던짐 -> 나중에 MSA로 전환할 때 코드 수정 없이 메시지 브큐(Kafka 등)만 연결하면 되는 구조라고 합니다
        eventPublisher.publishEvent(new PaymentCompletedEvent(orderId, userId, amount));

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPayments(UUID customerId, PaymentStatus status, Pageable pageable) {
        // 1. UserReadService를 통해 유저 존재 여부 확인 (MSA 인터페이스 지향)
        userReadService.validateUserExists(customerId);

        Pageable validatedPageable = validatePageSize(pageable);

        if (status != null) {
            return paymentRepository.findByUserIdAndPaymentStatusAndIsDeletedFalse(customerId, status, validatedPageable)
                    .map(PaymentResponse::from);
        }

        // 2. 결제 내역 조회 및 반환
        return paymentRepository.findByUserIdAndIsDeletedFalse(customerId, validatedPageable)
                .map(PaymentResponse::from);
    }

    private Pageable validatePageSize(Pageable pageable) {
        int size = pageable.getPageSize();
        if (size != 10 && size != 30 && size != 50) {
            return PageRequest.of(pageable.getPageNumber(), 10, pageable.getSort());
        }
        return pageable;
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID orderId, UUID userId) {
        Payment payment = paymentRepository.findByOrderIdAndUserIdAndIsDeletedFalse(orderId, userId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse cancelPayment(UUID paymentId, UUID userId) {
        Payment payment = paymentRepository.findByIdAndIsDeletedFalse(paymentId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 본인 확인
        if (!payment.getUserId().equals(userId)) {
            throw new OminBusinessException(ErrorCode.PAYMENT_UNAUTHORIZED);
        }

        // 이미 취소된 경우 체크
        if (payment.getPaymentStatus() == PaymentStatus.CANCELED) {
            throw new OminBusinessException(ErrorCode.PAYMENT_ALREADY_CANCELED);
        }

        // 결제 상태 변경
        payment.cancel();

        // 결제 취소 이벤트 발행 -> 주문 리스너가 듣고 주문을 취소함
        eventPublisher.publishEvent(new PaymentCanceledEvent(payment.getOrderId(), userId));

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByAdmin(UUID paymentId) {
        Payment payment = paymentRepository.findByIdAndIsDeletedFalse(paymentId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        return PaymentResponse.from(payment);
    }
}