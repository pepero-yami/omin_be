package com.sparta.omin.app.model.order.event;

import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.entity.status.OrderStatus;
import com.sparta.omin.app.model.order.service.OrderService;
import com.sparta.omin.app.model.payment.event.PaymentCanceledEvent;
import com.sparta.omin.app.model.payment.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderService orderService;

    // 결제 완료 시: 주문 상태 -> ACCEPTED
    @EventListener
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        Order order = orderService.getOrderEntity(event.orderId());
        order.completePayment(); // 또는 order.updateStatus(OrderStatus.ACCEPTED);
    }

    // 결제 취소 시: 주문 상태 -> CANCELED
    @EventListener
    @Transactional
    public void handlePaymentCanceled(PaymentCanceledEvent event) {
        Order order = orderService.getOrderEntity(event.orderId());
        order.updateStatus(OrderStatus.CANCELLED);
    }
}