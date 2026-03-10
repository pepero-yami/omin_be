package com.sparta.omin.payment.controller;

import com.sparta.omin.WebMvcTestBase;
import com.sparta.omin.app.controller.payment.PaymentController;
import com.sparta.omin.app.model.payment.service.PaymentService;
import com.sparta.omin.app.model.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@WebMvcTest(controllers = {PaymentController.class})
public class PaymentControllerHelper extends WebMvcTestBase {
    @MockitoBean
    protected PaymentService paymentService;

    @Autowired
    private WebApplicationContext context;

//    protected final String PAYMENTS_BASE_URL = "/api/v1/payments";

    @BeforeEach
    void setupSecurity() {
        // MockMvc가 시큐리티 필터를 거치도록 명시적으로 설정
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    protected User mockUser() {
        User user = User.builder()
                .email("test@test.com")
                .name("테스터")
                .nickname("테스트닉")
                .password("password123!")
                .build();

        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }
}