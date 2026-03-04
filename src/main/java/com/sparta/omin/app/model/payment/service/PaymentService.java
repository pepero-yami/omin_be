package com.sparta.omin.app.model.payment.service;

import com.sparta.omin.app.model.payment.dto.PaymentResponse;
import com.sparta.omin.app.model.payment.entity.Payment;
import com.sparta.omin.app.model.payment.repos.PaymentRepository;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentResponse getPayment(UUID orderId, UUID userId) {
        Payment payment = paymentRepository.findByOrderIdAndUserIdAndIsDeletedFalse(orderId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.PAYMENT_NOT_FOUND));
        return PaymentResponse.from(payment);
    }

    public PaymentResponse requestPayment(UUID orderId, UUID userId){
        return null;
    }

    public PaymentResponse cancelPayment(UUID paymentId, UUID userId) {
        return null;
    }


    /**
     * 관리자
     * TODO 삭제된 데이터는 볼 수 없도록 처리 (정책에 따라 수정가능)
     */
    public Page<PaymentResponse> getPayments(UUID customerId, Pageable pageable) {
        return paymentRepository.findByUserIdAndIsDeletedFalse(customerId, pageable)
                .map(PaymentResponse::from);
    }

    public PaymentResponse getPaymentByAdmin(UUID paymentId) {
        Payment payment = paymentRepository.findByIdAndIsDeletedFalse(paymentId)
                .orElseThrow(() -> new ApiException(ErrorCode.PAYMENT_NOT_FOUND));
        return PaymentResponse.from(payment);
    }
}
