package com.sparta.omin.app.model.payment.service;

import com.sparta.omin.app.model.payment.entity.Payment;
import com.sparta.omin.app.model.payment.repos.PaymentRepository;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class PaymentReadService {

    private final PaymentRepository paymentRepository;

    public Payment getPayment(UUID orderId) {
        return paymentRepository.findByOrderIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }
}
