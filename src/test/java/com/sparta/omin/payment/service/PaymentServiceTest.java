package com.sparta.omin.payment.service;

import com.sparta.omin.app.model.order.dto.OrderInternalDto;
import com.sparta.omin.app.model.order.service.OrderService;
import com.sparta.omin.app.model.payment.dto.PaymentResponse;
import com.sparta.omin.app.model.payment.entity.Payment;
import com.sparta.omin.app.model.payment.entity.PaymentStatus;
import com.sparta.omin.app.model.payment.event.PaymentCompletedEvent;
import com.sparta.omin.app.model.payment.repos.PaymentRepository;
import com.sparta.omin.app.model.payment.service.PaymentService;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment:Service")
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private OrderService orderService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @InjectMocks private PaymentService paymentService;

    @Nested
    @DisplayName("결제 요청 테스트")
    class RequestPayment {
        @Test
        @DisplayName("성공적으로 결제 요청을 생성한다")
        void requestPayment_success() {
            // Given
            UUID orderId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            OrderInternalDto orderDto = new OrderInternalDto(orderId, userId, 25000.0);

            given(orderService.getOrderForPayment(orderId)).willReturn(orderDto);
            given(paymentRepository.findByOrderIdAndIsDeletedFalse(orderId)).willReturn(Optional.empty());
            given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            PaymentResponse response = paymentService.requestPayment(orderId, userId, 25000.0);

            // Then
            assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.READY);
            assertThat(response.totalPrice()).isEqualTo(25000.0);
        }

        @Test
        @DisplayName("주문자가 다를 경우 예외가 발생한다")
        void requestPayment_userMismatch_throwsException() {
            // Given
            UUID orderId = UUID.randomUUID();
            UUID requestUserId = UUID.randomUUID();
            UUID actualOrderUserId = UUID.randomUUID();
            OrderInternalDto orderDto = new OrderInternalDto(orderId, actualOrderUserId, 25000.0);

            given(orderService.getOrderForPayment(orderId)).willReturn(orderDto);

            // When & Then
            assertThatThrownBy(() -> paymentService.requestPayment(orderId, requestUserId, 25000.0))
                    .isInstanceOf(OminBusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_ORDER_USER_MISMATCH);
        }
    }

    @Nested
    @DisplayName("결제 승인 테스트")
    class ConfirmPayment {
        @Test
        @DisplayName("결제 금액이 일치하면 승인 성공 및 이벤트를 발행한다")
        void confirmPayment_success() {
            // Given
            UUID orderId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Payment payment = Payment.create(orderId, userId, 25000.0);

            given(paymentRepository.findByOrderIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(payment));

            // When
            paymentService.confirmPayment(orderId, "toss-key", 25000.0, userId);

            // Then
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
            verify(eventPublisher, times(1)).publishEvent(any(PaymentCompletedEvent.class));
        }

        @Test
        @DisplayName("금액이 일치하지 않으면 승인이 실패한다")
        void confirmPayment_amountMismatch_throwsException() {
            // Given
            UUID orderId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Payment payment = Payment.create(orderId, userId, 25000.0);

            given(paymentRepository.findByOrderIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(payment));

            // When & Then
            assertThatThrownBy(() -> paymentService.confirmPayment(orderId, "key", 100.0, userId))
                    .isInstanceOf(OminBusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_AMOUNT_MISMATCH);

            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        }
    }
}
