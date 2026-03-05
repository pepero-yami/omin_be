package com.sparta.omin.address.controller;

import com.sparta.omin.app.controller.address.AddressController;
import com.sparta.omin.app.model.address.service.AddressService;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.region.WebMvcTestBase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = {AddressController.class})
public abstract class AddressControllerHelper extends WebMvcTestBase {

    @MockitoBean
    protected AddressService addressService;

    protected final String ADDRESS_BASE_URL = "/api/v1/me/addresses";
    protected final String ADDRESS_ID_URL = "/api/v1/me/addresses/%s";
    protected final String ADDRESS_DEFAULT_URL = "/api/v1/me/addresses/%s/default";

    protected final String ADDRESS_CREATE_FIXTURE = """
            {
                "nickname": "우리집",
                "roadAddress": "서울특별시 강남구 테헤란로 427",
                "shippingDetailAddress": "10층",
                "isDefault": true
            }
            """;

    protected void mockUser() {
        User user = User.builder()
                .email("test@test.com")
                .nickname("테스터")
                .password("password123!")
                .name("홍길동")
                .build();

        // SecurityContext에 인증 정보 강제 주입 - 로그인된 상태로 간주되도록
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}