package com.sparta.omin.app.controller.payment;

import com.sparta.omin.app.model.payment.dto.PaymentConfirmRequest;
import com.sparta.omin.app.model.payment.dto.PaymentRequest;
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

    //고객용 API
    // 결제 요청 (READY 생성)
    @PostMapping("/payments/request")
    public ResponseEntity<PaymentResponse> requestPayment(@RequestBody PaymentRequest request,
                                                          @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paymentService.requestPayment(request.orderId(), user.getId(), request.amount()));
    }

    // 결제 승인 (Toss Confirm처럼!)
    @PostMapping("/payments/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @RequestBody PaymentConfirmRequest request,
            @AuthenticationPrincipal User user) {
        // 현재 로그인한 유저의 ID를 서비스로 넘겨서 결제 데이터 소유권 확인함!
        return ResponseEntity.ok(paymentService.confirmPayment(request.orderId(), request.paymentKey(), request.amount(), user.getId()));
    }

    // 내 주문의 결제 내역 단건 조회
    @GetMapping("/payments/{orderId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID orderId,
                                                      @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paymentService.getPayment(orderId, user.getId()));
    }

    // 결제 취소 요청
    @PatchMapping("/payments/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable UUID paymentId,
                                                         @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paymentService.cancelPayment(paymentId, user.getId()));
    }

    // 관리자 전용 API
    // 특정 고객의 모든 결제 내역 조회 (페이지네이션)
    @GetMapping("/admin/payments")
    public ResponseEntity<Page<PaymentResponse>> getPayments(@RequestParam UUID customerId,
                                                             @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                                                             Pageable pageable) {
        return ResponseEntity.ok(paymentService.getPayments(customerId, pageable));
    }

    // 결제 ID로 상세 조회
    @GetMapping("/admin/payments/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentByAdmin(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentByAdmin(paymentId));
    }
}