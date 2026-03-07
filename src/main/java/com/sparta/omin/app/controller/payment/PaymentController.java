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

    /**
     * 고객 API
     */
    @GetMapping("/payments/{orderId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID orderId,
                                                      @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paymentService.getPayment(orderId, user.getId()));
    }

    // 1. 결제 시작 (READY 생성)
    @PostMapping("/payments/request")
    public ResponseEntity<PaymentResponse> requestPayment(@RequestBody PaymentRequest request,
                                                          @AuthenticationPrincipal User user){
        return ResponseEntity.ok(paymentService.requestPayment(request.orderId(), user.getId(), request.amount()));
    }

    // 2. 결제 승인 (Toss Confirm처럼!)
    @PostMapping("/payments/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(@RequestBody PaymentConfirmRequest request) {
        return ResponseEntity.ok(paymentService.confirmPayment(request.orderId(), request.paymentKey(), request.amount()));
    }

    @DeleteMapping("/payments/{paymentId}")
    public ResponseEntity<PaymentResponse> requestPaymentCancellation(@PathVariable UUID paymentId,
                                                                      @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paymentService.cancelPayment(paymentId, user.getId()));
    }

    /**
     * 관리자 API
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

    // 임시!!! 리스너가 미완성일 때 수동으로 주문 상태 변경 테스트를 할 때 사용... orderRepository를 통해 직접 상태를 바꾸기
    @PostMapping("/test/order-status/{orderId}")
    public ResponseEntity<String> forceUpdateOrderStatus(@PathVariable UUID orderId) {

        return ResponseEntity.ok("테스트용 주문 상태 변경 호출 완료");
    }

    //TODO 멱등성 처리: 같은 주문에 대해 실수로 결제 요청이 두 번 올 경우를 대비한 로직 (현재 ifPresent로 체크 중이지만 좀 더 정교하게?)

    //TODO 결제 유효 시간: READY 상태로 생성된 결제가 일정 시간(예: 30분) 내에 CONFIRM되지 않으면 자동으로 FAILED 처리하는 스케줄러?

    //TODO 권한 체크 상세: 현재 AuthenticationPrincipal을 통해 본인의 결제 내역만 보게 되어 있으나, confirm 단계에서도 주문의 소유자와 현재 로그인한 유저가 일치하는지 한 번 더 체크?
}