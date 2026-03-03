package com.sparta.omin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.omin.app.controller.store.StoreController;
import com.sparta.omin.app.model.store.dto.StoreCreateRequest;
import com.sparta.omin.app.model.store.dto.StoreResponse;
import com.sparta.omin.app.model.store.entity.Category;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.store.entity.StoreImage;
import com.sparta.omin.app.model.store.service.StoreService;
import com.sparta.omin.common.config.SecurityConfig;
import com.sparta.omin.common.error.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoreController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
public class StoreApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StoreService storeService;

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

        Store store = request.toEntity();

        request.images().stream()
                .map(StoreImage::new)
                .forEach(store::addImage);

        StoreResponse response = StoreResponse.of(store);

        given(storeService.registerStore(any(StoreCreateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.regionId").value(response.regionId().toString()))
                .andExpect(jsonPath("$.data.category").value(response.category().toString()))
                .andExpect(jsonPath("$.data.roadAddress").value(response.roadAddress()))
                .andExpect(jsonPath("$.data.images[0].imageUrl").value("http://example.com/image1.jpg"))
                .andExpect(jsonPath("$.data.images[0].sequence").value(1))
                .andExpect(jsonPath("$.data.images[1].imageUrl").value("http://example.com/image2.jpg"))
                .andExpect(jsonPath("$.data.images[1].sequence").value(2));
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
        mockMvc.perform(post("/api/v1/stores/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
