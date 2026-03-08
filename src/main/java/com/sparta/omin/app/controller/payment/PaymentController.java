package com.sparta.omin.app.controller.payment;

import com.sparta.omin.app.model.payment.dto.PaymentResponse;
import com.sparta.omin.app.model.payment.service.PaymentService;
import com.sparta.omin.app.model.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 고객
     */
    @GetMapping("/payments/{orderId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID orderId,
                                                      @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paymentService.getPayment(orderId, user.getId()));
    }

    @PostMapping("/payments/{orderId}")
    public ResponseEntity<PaymentResponse> requestPayment(@PathVariable UUID orderId,
                                                          @AuthenticationPrincipal User user){
        return ResponseEntity.ok(paymentService.requestPayment(orderId, user.getId()));
    }

    @DeleteMapping("/payments/{paymentId}")
    public ResponseEntity<PaymentResponse> requestPaymentCancellation(@PathVariable UUID paymentId,
                                                                @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paymentService.cancelPayment(paymentId, user.getId()));
    }

    /**
     * 관리자
     */
    @GetMapping("/admin/payments")
    public ResponseEntity<Page<PaymentResponse>> getPayments(@RequestParam UUID customerId,
                                                             @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                                                             Pageable pageable) {
        return ResponseEntity.ok(paymentService.getPayments(customerId, pageable));
    }

    @GetMapping("/admin/payments/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentByAdmin(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentByAdmin(paymentId));
    }
}
