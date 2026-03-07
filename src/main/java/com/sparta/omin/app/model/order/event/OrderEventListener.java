package com.sparta.omin.app.model.order.event;

import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.repos.OrderRepository;
import com.sparta.omin.app.model.payment.event.PaymentCompletedEvent;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderRepository orderRepository;

    @EventListener
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new OminBusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 결제가 완료되면 주문 상태를 PENDING -> ACCEPTED로 변경 (완성된 코드의 메서드명에 맞춰 수정 필요)
        // 현재 Order 엔티티에는 status 변경 로직이 없음 -> 도입할거면 추가 필요!
        // order.completePayment();
    }
}