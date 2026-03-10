//package com.sparta.omin.payment.controller;
//
//import com.sparta.omin.app.model.payment.dto.PaymentConfirmRequest;
//import com.sparta.omin.app.model.payment.dto.PaymentRequest;
//import com.sparta.omin.app.model.payment.dto.PaymentResponse;
//import com.sparta.omin.app.model.payment.entity.PaymentMethod;
//import com.sparta.omin.app.model.payment.entity.PaymentStatus;
//import com.sparta.omin.app.model.user.entity.User;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.data.domain.Page;
//import org.springframework.http.MediaType;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.BDDMockito.given;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@Disabled("테스트코드 수정중")
//@DisplayName("Payment:Controller")
//class PaymentControllerTest extends PaymentControllerHelper {
//
//    @Test
//    @DisplayName("결제 요청 성공 (200 OK)")
//    void requestPayment_success() throws Exception {
//        // Given
//        User user = mockUser();
//        UUID orderId = UUID.randomUUID();
//        PaymentResponse response = new PaymentResponse(
//                UUID.randomUUID(), orderId, PaymentMethod.CREDIT_CARD, 25000.0, LocalDateTime.now(), PaymentStatus.READY, null);
//
//        given(paymentService.requestPayment(eq(orderId), any(), anyDouble())).willReturn(response);
//
//        // When & Then: POST 요청 경로 확인
//        mockMvc.perform(post("/api/v1/payments/request")
//                        .with(user(user))
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(new PaymentRequest(orderId, 25000.0))))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.paymentStatus").value("READY"));
//    }
//
//    @Test
//    @DisplayName("결제 승인 성공 (200 OK)")
//    void confirmPayment_success() throws Exception {
//        // Given
//        User user = mockUser();
//        UUID orderId = UUID.randomUUID();
//        PaymentResponse response = new PaymentResponse(
//                UUID.randomUUID(), orderId, PaymentMethod.CREDIT_CARD, 25000.0, LocalDateTime.now(), PaymentStatus.SUCCESS, "key");
//
//        given(paymentService.confirmPayment(eq(orderId), anyString(), anyDouble(), any())).willReturn(response);
//
//        // [회색 import 해결] PaymentConfirmRequest 명시적 사용
//        PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest(orderId, "key", 25000.0);
//
//        // When & Then
//        mockMvc.perform(post("/api/v1/payments/confirm")
//                        .with(user(user))
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(confirmRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"));
//    }
//
//    @Test
//    @DisplayName("관리자: 고객 결제 내역 조회 성공 (200 OK)")
//    void getPaymentsByAdmin_success() throws Exception {
//        // Given: 관리자용 경로 (/admin/...)
//        UUID customerId = UUID.randomUUID();
//        given(paymentService.getPayments(eq(customerId), any())).willReturn(Page.empty());
//
//        // When & Then
//        mockMvc.perform(get("/api/v1/admin/payments")
//                        .param("customerId", customerId.toString())
//                        .with(user(mockUser())))
//                .andDo(print())
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    @DisplayName("결제 취소 성공 (200 OK)")
//    void cancelPayment_success() throws Exception {
//        // Given
//        User user = mockUser();
//        UUID paymentId = UUID.randomUUID();
//        PaymentResponse response = new PaymentResponse(
//                paymentId, UUID.randomUUID(), PaymentMethod.CREDIT_CARD, 25000.0, LocalDateTime.now(), PaymentStatus.CANCELED, "key");
//
//        given(paymentService.cancelPayment(eq(paymentId), any())).willReturn(response);
//
//        // When & Then
//        mockMvc.perform(patch("/api/v1/payments/" + paymentId + "/cancel")
//                        .with(user(user))
//                        .with(csrf()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.paymentStatus").value("CANCELED"));
//    }
//}