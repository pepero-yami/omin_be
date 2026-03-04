package com.sparta.omin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.omin.app.controller.store.StoreController;
import com.sparta.omin.app.model.store.dto.StoreCreateRequest;
import com.sparta.omin.app.model.store.dto.StoreResponse;
import com.sparta.omin.app.model.store.dto.StoreUpdateRequest;
import com.sparta.omin.app.model.store.entity.Category;
import com.sparta.omin.app.model.store.service.StoreService;
import com.sparta.omin.app.security.jwt.JwtUtil;
import com.sparta.omin.common.error.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoreController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class StoreApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StoreService storeService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("가게 생성(이미지 포함) 성공")
    void createStoreSuccess() throws Exception {
        // given
        UUID regionId = UUID.fromString("d075adff-e07a-4f1a-94d0-155d7244e09e");
        StoreCreateRequest request = new StoreCreateRequest(
                regionId,
                Category.KOREAN,
                "Test Store",
                "Test Road Address",
                "Test Detail Address",
                new BigDecimal("127.0276"),
                new BigDecimal("37.4979"),
                List.of("http://example.com/image1.jpg", "http://example.com/image2.jpg")
        );

        StoreResponse response = StoreResponse.builder().build(); // Simplified for test
        given(storeService.registerStore(any(StoreCreateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("가게 생성 실패 - 유효성 검사")
    void createStoreValidationFail() throws Exception {
        // given
        StoreCreateRequest request = new StoreCreateRequest(
                null,
                null,
                "",
                "",
                "",
                null,
                null,
                Collections.emptyList()
        );

        // when & then
        mockMvc.perform(post("/api/v1/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("가게 삭제 성공")
    void deleteStoreSuccess() throws Exception {
        // given
        UUID storeId = UUID.randomUUID();
        doNothing().when(storeService).deleteStore(any(UUID.class));

        // when & then
        mockMvc.perform(delete("/api/v1/stores/{storeId}", storeId))
                .andDo(print())
                .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("가게 수정 성공")
    void updateStoreSuccess() throws Exception {
        // given
        UUID storeId = UUID.randomUUID();
        UUID regionId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        StoreUpdateRequest request = new StoreUpdateRequest(
                regionId,
                Category.CHINESE,
                "Updated Store",
                "Updated Road Address",
                "Updated Detail Address",
                new BigDecimal("127.0277"),
                new BigDecimal("37.4978"),
                List.of(new StoreUpdateRequest.StoreImageRequest("http://example.com/new_image.jpg", null))
        );

        StoreResponse response = StoreResponse.builder()
                .id(storeId)
                .ownerId(ownerId)
                .regionId(regionId)
                .category(Category.CHINESE)
                .name("Updated Store")
                .roadAddress("Updated Road Address")
                .detailAddress("Updated Detail Address")
                .latitude(new BigDecimal("37.4978"))
                .longitude(new BigDecimal("127.0277"))
                .images(Collections.emptyList())
                .build();

        given(storeService.modifyStore(any(StoreUpdateRequest.class), eq(storeId))).willReturn(response);

        // when & then
        mockMvc.perform(put("/api/v1/stores/{storeId}", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.category").value(Category.CHINESE.toString()))
                .andExpect(jsonPath("$.data.roadAddress").value("Updated Road Address"));
    }

    @Test
    @DisplayName("가게 수정 실패 - 유효성 검사")
    void updateStoreValidationFail() throws Exception {
        // given
        UUID storeId = UUID.randomUUID();
        StoreUpdateRequest request = new StoreUpdateRequest(
                null,
                null,
                "",
                "",
                "",
                null,
                null,
                Collections.emptyList()
        );

        // when & then
        mockMvc.perform(put("/api/v1/stores/{storeId}", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
