package com.sparta.omin.payment.controller;

import com.sparta.omin.app.model.payment.dto.PaymentConfirmRequest;
import com.sparta.omin.app.model.payment.dto.PaymentRequest;
import com.sparta.omin.app.model.payment.dto.PaymentResponse;
import com.sparta.omin.app.model.payment.entity.PaymentMethod;
import com.sparta.omin.app.model.payment.entity.PaymentStatus;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Payment:Controller")
//@AutoConfigureMockMvc(addFilters = false) // 필터 제외로 시큐리티 복잡성 회피
        // addFilters = false를 제거! (시큐리티가 작동해야 @AuthenticationPrincipal이 바인딩됨)
class PaymentControllerTest extends PaymentControllerHelper {

    @Test
    @DisplayName("결제 요청 성공 (200 OK)")
    void requestPayment_success() throws Exception {
        // Given
        User user = mockUser(); // Helper에서 ID가 주입된 실제 User 객체 생성
        UUID orderId = UUID.randomUUID();
        PaymentResponse response = new PaymentResponse(
                UUID.randomUUID(), orderId, PaymentMethod.CREDIT_CARD, 25000.0, LocalDateTime.now(), PaymentStatus.READY, null);

        given(paymentService.requestPayment(eq(orderId), any(), anyDouble())).willReturn(response);

        PaymentRequest request = new PaymentRequest(orderId, 25000.0);

        // When & Then
        mockMvc.perform(post(PAYMENTS_BASE_URL + "/request")
                        .with(user(user)) //시큐리티 컨텍스트 주입
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("READY"))
                .andExpect(jsonPath("$.totalPrice").value(25000.0));
    }

    @Test
    @DisplayName("결제 승인 성공 (200 OK)")
    void confirmPayment_success() throws Exception {
        // Given
        User user = mockUser();
        UUID orderId = UUID.randomUUID();
        PaymentResponse response = new PaymentResponse(
                UUID.randomUUID(), orderId, PaymentMethod.CREDIT_CARD, 25000.0, LocalDateTime.now(), PaymentStatus.SUCCESS, "toss-confirm-key");

        given(paymentService.confirmPayment(eq(orderId), anyString(), anyDouble(), any())).willReturn(response);

        PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest(orderId, "toss-confirm-key", 25000.0);

        // When & Then
        mockMvc.perform(post(PAYMENTS_BASE_URL + "/confirm")
                        .with(user(user))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.paymentKey").value("toss-confirm-key"));
    }

    @Test
    @DisplayName("결제 금액 불일치 시 승인 실패 (400 Bad Request)")
    void confirmPayment_amountMismatch_returns400() throws Exception {
        // Given
        User user = mockUser();
        UUID orderId = UUID.randomUUID();

        // 서비스에서 금액 불일치 예외를 던지도록 설정
        given(paymentService.confirmPayment(any(), any(), anyDouble(), any()))
                .willThrow(new OminBusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH));

        PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest(orderId, "wrong-key", 100.0);

        // When & Then
        mockMvc.perform(post(PAYMENTS_BASE_URL + "/confirm")
                        .with(user(user))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("PAYMENT_AMOUNT_MISMATCH"));
    }

    @Test
    @DisplayName("권한 없는 사용자가 결제 정보 조회 시 실패 (404 Not Found)")
    void getPayment_unauthorized_returns404() throws Exception {
        // Given
        User user = mockUser();
        UUID orderId = UUID.randomUUID();

        // 본인 결제가 아니면 서비스에서 NOT_FOUND(또는 UNAUTHORIZED)를 던짐
        given(paymentService.getPayment(any(), any()))
                .willThrow(new OminBusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // When & Then
        mockMvc.perform(get(PAYMENTS_BASE_URL + "/" + orderId)
                        .with(user(user)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("PAYMENT_NOT_FOUND"));
    }
}