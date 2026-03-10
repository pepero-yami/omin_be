package com.sparta.omin.payment.service;

import com.sparta.omin.app.model.order.dto.OrderInternalDto;
import com.sparta.omin.app.model.order.service.OrderReadService;
import com.sparta.omin.app.model.payment.dto.PaymentResponse;
import com.sparta.omin.app.model.payment.entity.Payment;
import com.sparta.omin.app.model.payment.entity.PaymentStatus;
import com.sparta.omin.app.model.payment.event.PaymentCanceledEvent;
import com.sparta.omin.app.model.payment.event.PaymentCompletedEvent;
import com.sparta.omin.app.model.payment.repos.PaymentRepository;
import com.sparta.omin.app.model.payment.service.PaymentService;
import com.sparta.omin.app.model.user.service.UserReadService;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment:Service")
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private OrderReadService orderReadService;
    @Mock private UserReadService userReadService; // 유저 존재 확인을 위해 필요
    @Mock private ApplicationEventPublisher eventPublisher; // 이벤트 발행 검증을 위해 필요
    @InjectMocks private PaymentService paymentService;

    @Nested
    @DisplayName("결제 요청(requestPayment) 테스트")
    class RequestPayment {
        @Test
        @DisplayName("성공: 새로운 결제 요청을 생성한다")
        void requestPayment_success() {
            // Given: 테스트 데이터 및 주문 서비스 응답 설정
            UUID orderId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            double amount = 25000.0;
            OrderInternalDto orderDto = new OrderInternalDto(orderId, userId, amount);

            given(orderReadService.getOrderForPayment(orderId)).willReturn(orderDto);
            given(paymentRepository.findByOrderIdAndIsDeletedFalse(orderId)).willReturn(Optional.empty());
            given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When: 결제 요청 실행
            PaymentResponse response = paymentService.requestPayment(orderId, userId, amount);

            // Then: 결제 상태가 READY인지 확인
            assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.READY);
            verify(paymentRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("성공: 기존 READY 건이 있으면 새로 생성하지 않고 반환한다 (멱등성)")
        void requestPayment_reuse_ready() {
            // Given: 이미 READY 상태인 결제가 존재하는 상황
            UUID orderId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            double amount = 25000.0;
            OrderInternalDto orderDto = new OrderInternalDto(orderId, userId, amount);
            Payment existingPayment = Payment.create(orderId, userId, amount);

            given(orderReadService.getOrderForPayment(orderId)).willReturn(orderDto);
            given(paymentRepository.findByOrderIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(existingPayment));

            // When: 다시 결제 요청
            PaymentResponse response = paymentService.requestPayment(orderId, userId, amount);

            // Then: 새로 save하지 않고 기존 데이터 반환 확인
            assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.READY);
            verify(paymentRepository, times(0)).save(any());
        }

        @Test
        @DisplayName("실패: 주문 금액과 요청 금액이 다르면 예외가 발생한다")
        void requestPayment_amountMismatch_throwsException() {
            // Given: 주문 금액은 25000원인데 요청은 1000원인 상황
            UUID orderId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            OrderInternalDto orderDto = new OrderInternalDto(orderId, userId, 25000.0);

            given(orderReadService.getOrderForPayment(orderId)).willReturn(orderDto);

            // When & Then: 비즈니스 예외 발생 검증
            assertThatThrownBy(() -> paymentService.requestPayment(orderId, userId, 1000.0))
                    .isInstanceOf(OminBusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    @Nested
    @DisplayName("결제 승인 및 취소 테스트")
    class PaymentProcess {
        @Test
        @DisplayName("성공: 승인 처리 후 완료 이벤트를 발행한다")
        void confirmPayment_success() {
            // Given
            UUID orderId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            double amount = 25000.0;
            Payment payment = Payment.create(orderId, userId, amount);

            given(paymentRepository.findByOrderIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(payment));

            // When
            paymentService.confirmPayment(orderId, "toss-key", amount, userId);

            // Then: 상태 변경 확인 및 이벤트 발행 검증
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
            verify(eventPublisher, times(1)).publishEvent(any(PaymentCompletedEvent.class));
        }

        @Test
        @DisplayName("성공: 결제 취소 후 취소 이벤트를 발행한다")
        void cancelPayment_success() {
            // Given
            UUID paymentId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Payment payment = Payment.create(UUID.randomUUID(), userId, 25000.0);

            given(paymentRepository.findByIdAndIsDeletedFalse(paymentId)).willReturn(Optional.of(payment));

            // When
            paymentService.cancelPayment(paymentId, userId);

            // Then: 취소 상태 확인 및 CanceledEvent 발행 검증 (회색 import 해결)
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED);
            verify(eventPublisher, times(1)).publishEvent(any(PaymentCanceledEvent.class));
        }

        @Test
        @DisplayName("관리자 조회: 유저 존재 확인 로직이 실행되는지 검증한다")
        void getPayments_success() {
            // Given
            UUID customerId = UUID.randomUUID();
            willDoNothing().given(userReadService).validateUserExists(customerId);
            given(paymentRepository.findByUserIdAndIsDeletedFalse(eq(customerId), any())).willReturn(Page.empty());

            // When: 관리자가 유저 내역 조회
            paymentService.getPayments(customerId, Pageable.unpaged());

            // Then: 유저 도메인 서비스가 정상 호출되었는지 확인
            verify(userReadService, times(1)).validateUserExists(customerId);
        }
    }
}