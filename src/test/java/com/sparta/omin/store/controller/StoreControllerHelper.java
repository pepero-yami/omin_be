package com.sparta.omin.store.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.omin.app.controller.store.StoreController;
import com.sparta.omin.app.model.store.code.Category;
import com.sparta.omin.app.model.store.code.Status;
import com.sparta.omin.app.model.store.dto.StoreListResponse;
import com.sparta.omin.app.model.store.dto.StoreResponse;
import com.sparta.omin.app.model.store.service.StoreService;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.app.model.user.service.UserDetailsServiceImpl;
import com.sparta.omin.app.security.jwt.JwtUtil;
import com.sparta.omin.common.error.GlobalExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@WebMvcTest(controllers = {StoreController.class})
@Import(GlobalExceptionHandler.class)
public abstract class StoreControllerHelper{

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected JwtUtil jwtUtil;

    @MockitoBean
    protected UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    protected StoreService storeService;

    protected static final UUID PENDING_STORE_ID = UUID.fromString("a6836031-57b1-427b-9796-5eee3a07eb41");
    protected static final UUID NON_PENDING_STORE_ID = UUID.fromString("51badc9e-319a-4764-816d-76923ed64d75");

    protected static final String STORES_BASE_URL = "/api/v1/stores";
    protected static final String STORE_URL = "/api/v1/stores/%s";
    protected static final String STORE_ADMIN_URL = "/api/v1/stores/%s/admin";
    protected static final String STORE_OWNER_URL = "/api/v1/stores/%s/owner";
    protected static final String STORE_OWNER_MY_URL = "/api/v1/stores/owner/my";
    protected static final String STORE_ADMIN_PENDING_URL = "/api/v1/stores/admin/pending";

    protected void mockUser() {
        User user = User.builder()
                .email("owner@test.com")
                .nickname("점주")
                .password("password123!")
                .name("홍길동")
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    protected StoreResponse buildStoreResponse(UUID storeId, String name, Status status, Category category) {
        return StoreResponse.builder()
                .id(storeId)
                .ownerId(UUID.randomUUID())
                .category(category)
                .name(name)
                .roadAddress("서울특별시 강남구 테헤란로 427")
                .detailAddress("1층")
                .status(status)
                .images(List.of(new StoreResponse.StoreImageResponse(UUID.randomUUID(), "store.jpg", 1)))
                .build();
    }

    protected StoreListResponse buildStoreListResponse(UUID storeId, String name, Status status) {
        return StoreListResponse.builder()
                .id(storeId)
                .name(name)
                .category(Category.KOREAN)
                .status(status)
                .roadAddress("서울특별시 강남구 테헤란로 427")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
