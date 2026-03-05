package com.sparta.omin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.omin.app.controller.store.StoreController;
import com.sparta.omin.app.model.store.dto.StoreCreateRequest;
import com.sparta.omin.app.model.store.dto.StoreResponse;
import com.sparta.omin.app.model.store.dto.StoreStatusUpdateRequest;
import com.sparta.omin.app.model.store.dto.StoreUpdateRequest;
import com.sparta.omin.app.model.store.entity.Category;
import com.sparta.omin.app.model.store.entity.Status;
import com.sparta.omin.app.model.store.service.StoreService;
import com.sparta.omin.app.model.user.service.UserDetailsServiceImpl;
import com.sparta.omin.app.security.jwt.JwtUtil;
import com.sparta.omin.common.error.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = StoreController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class StoreApiTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    StoreService storeService;

    @MockBean
    JwtUtil jwtUtil;

    @MockBean
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    ObjectMapper objectMapper;

    // PENDING 상태인 가게 ID
    static final UUID PENDING_STORE_ID = UUID.fromString("a6836031-57b1-427b-9796-5eee3a07eb41");
    // PENDING이 아닌 가게 ID
    static final UUID NON_PENDING_STORE_ID = UUID.fromString("51badc9e-319a-4764-816d-76923ed64d75");

    static final UUID REGION_ID = UUID.fromString("31bb6096-82e2-4919-9188-7a1167c68bd0");

    @Nested
    @DisplayName("POST /api/v1/stores - 가게 생성")
    class CreateStore {

        @Test
        @DisplayName("성공: 데이터와 이미지 파일을 함께 전송하면 201 반환")
        void createStore_returns201() throws Exception {
            StoreCreateRequest request = new StoreCreateRequest(
                    REGION_ID, Category.KOREAN, "후와후와",
                    "서울특별시 강남구 테헤란로 427", "1층",
                    new BigDecimal("127.050000"), new BigDecimal("37.500000")
            );

            MockMultipartFile dataFile = new MockMultipartFile(
                    "data", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
            );
            MockMultipartFile imageFile = new MockMultipartFile(
                    "images", "store.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content".getBytes()
            );

            StoreResponse response = StoreResponse.builder()
                    .id(PENDING_STORE_ID)
                    .ownerId(UUID.randomUUID())
                    .regionId(REGION_ID)
                    .category(Category.KOREAN)
                    .name("후와후와")
                    .roadAddress("서울특별시 강남구 테헤란로 427")
                    .detailAddress("1층")
                    .status(Status.PENDING)
                    .longitude(new BigDecimal("127.050000"))
                    .latitude(new BigDecimal("37.500000"))
                    .images(List.of(new StoreResponse.StoreImageResponse(UUID.randomUUID(),"store.jpg",1)))
                    .build();

            given(storeService.registerStore(any(), any(), any())).willReturn(response);

            mockMvc.perform(multipart("/api/v1/stores")
                            .file(dataFile)
                            .file(imageFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.id").value(PENDING_STORE_ID.toString()))
                    .andExpect(jsonPath("$.data.name").value("후와후와"))
                    .andExpect(jsonPath("$.data.status").value("PENDING"));
        }

        @Test
        @DisplayName("실패: 필수 데이터 누락 시 400 반환")
        void createStore_validationError_returns400() throws Exception {
            StoreCreateRequest invalidRequest = new StoreCreateRequest(
                    null, null, "", "", "", null, null
            );

            MockMultipartFile dataFile = new MockMultipartFile(
                    "data", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsString(invalidRequest).getBytes(StandardCharsets.UTF_8)
            );
            MockMultipartFile imageFile = new MockMultipartFile(
                    "images", "store.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content".getBytes()
            );

            mockMvc.perform(multipart("/api/v1/stores")
                            .file(dataFile)
                            .file(imageFile))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.details").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/stores/{storeId} - 가게 조회")
    class GetStore {

        @Test
        @DisplayName("성공: 존재하는 가게 조회 시 200 반환")
        void getStore_returns200() throws Exception {
            StoreResponse response = StoreResponse.builder()
                    .id(NON_PENDING_STORE_ID)
                    .ownerId(UUID.randomUUID())
                    .regionId(REGION_ID)
                    .category(Category.KOREAN)
                    .name("후와후와")
                    .roadAddress("서울특별시 강남구 테헤란로 427")
                    .detailAddress("1층")
                    .status(Status.OPENED)
                    .longitude(new BigDecimal("127.050000"))
                    .latitude(new BigDecimal("37.500000"))
                    .images(List.of(new StoreResponse.StoreImageResponse(UUID.randomUUID(),"store.jpg",1)))
                    .build();

            given(storeService.findStore(NON_PENDING_STORE_ID)).willReturn(response);

            mockMvc.perform(get("/api/v1/stores/{storeId}", NON_PENDING_STORE_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.id").value(NON_PENDING_STORE_ID.toString()))
                    .andExpect(jsonPath("$.data.name").value("후와후와"));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 가게 조회 시 404 반환")
        void getStore_notFound_returns404() throws Exception {
            UUID notExistId = UUID.randomUUID();
            given(storeService.findStore(notExistId))
                    .willThrow(new IllegalArgumentException("해당 가게를 찾을 수 없습니다."));

            mockMvc.perform(get("/api/v1/stores/{storeId}", notExistId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.details").exists());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/stores/{storeId} - 가게 수정")
    class UpdateStore {

        @Test
        @DisplayName("성공: 정보 수정 및 이미지 추가 시 200 반환")
        void updateStore_returns200() throws Exception {
            StoreUpdateRequest request = new StoreUpdateRequest(
                    REGION_ID, Category.CHINESE, "수정후와후와",
                    "서울특별시 서초구 강남대로 465", "2층",
                    new BigDecimal("128.000000"), new BigDecimal("38.000000"),
                    List.of(new StoreUpdateRequest.StoreImageRequest(null, true))
            );

            MockMultipartFile dataFile = new MockMultipartFile(
                    "data", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
            );
            MockMultipartFile newImageFile = new MockMultipartFile(
                    "newImages", "new.jpg", MediaType.IMAGE_JPEG_VALUE, "new-content".getBytes()
            );

            StoreResponse response = StoreResponse.builder()
                    .id(NON_PENDING_STORE_ID)
                    .ownerId(UUID.randomUUID())
                    .regionId(REGION_ID)
                    .category(Category.CHINESE)
                    .name("수정후와후와")
                    .roadAddress("서울특별시 서초구 강남대로 465")
                    .detailAddress("2층")
                    .status(Status.OPENED)
                    .longitude(new BigDecimal("128.000000"))
                    .latitude(new BigDecimal("38.000000"))
                    .images(List.of(new StoreResponse.StoreImageResponse(UUID.randomUUID(),"new.jpg",1)))
                    .build();

            given(storeService.modifyStore(eq(NON_PENDING_STORE_ID), any(), any(), any()))
                    .willReturn(response);

            mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/stores/{storeId}", NON_PENDING_STORE_ID)
                            .file(dataFile)
                            .file(newImageFile))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.id").value(NON_PENDING_STORE_ID.toString()))
                    .andExpect(jsonPath("$.data.name").value("수정후와후와"))
                    .andExpect(jsonPath("$.data.category").value("CHINESE"));
        }

        @Test
        @DisplayName("실패: 권한 없는 사용자가 수정 시도하면 403 반환")
        void updateStore_accessDenied_returns403() throws Exception {
            StoreUpdateRequest request = new StoreUpdateRequest(
                    REGION_ID, Category.KOREAN, "테스트",
                    "서울특별시 강남구 테헤란로 427", "1층",
                    new BigDecimal("127.050000"), new BigDecimal("37.500000"),
                    List.of(new StoreUpdateRequest.StoreImageRequest(null, true))
            );

            MockMultipartFile dataFile = new MockMultipartFile(
                    "data", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
            );
            MockMultipartFile newImageFile = new MockMultipartFile(
                    "newImages", "noImg.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]
            );

            given(storeService.modifyStore(eq(NON_PENDING_STORE_ID), any(), any(), any()))
                    .willThrow(new AccessDeniedException("해당 가게에 대한 권한이 없습니다"));

            mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/stores/{storeId}", NON_PENDING_STORE_ID)
                            .file(dataFile)
                            .file(newImageFile))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.details").exists());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 가게 수정 시 404 반환")
        void updateStore_notFound_returns404() throws Exception {
            UUID notExistId = UUID.randomUUID();
            StoreUpdateRequest request = new StoreUpdateRequest(
                    REGION_ID, Category.KOREAN, "우리가게없는뎅본점",
                    "서울특별시 강남구 테헤란로 427", "1층",
                    new BigDecimal("127.050000"), new BigDecimal("37.500000"),
                    List.of(new StoreUpdateRequest.StoreImageRequest(null, true))
            );

            MockMultipartFile dataFile = new MockMultipartFile(
                    "data", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
            );
            MockMultipartFile newImageFile = new MockMultipartFile(
                    "newImages", "noImg.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]
            );

            given(storeService.modifyStore(eq(notExistId), any(), any(), any()))
                    .willThrow(new IllegalArgumentException("해당 가게를 찾을 수 없습니다."));

            mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/stores/{storeId}", notExistId)
                            .file(dataFile)
                            .file(newImageFile))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.details").exists());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/stores/{storeId} - 가게 삭제")
    class DeleteStore {

        @Test
        @DisplayName("성공: 가게 삭제 시 204 반환")
        void deleteStore_returns204() throws Exception {
            willDoNothing().given(storeService).deleteStore(eq(NON_PENDING_STORE_ID), any());

            mockMvc.perform(delete("/api/v1/stores/{storeId}", NON_PENDING_STORE_ID))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("실패: 권한 없는 사용자가 삭제 시도하면 403 반환")
        void deleteStore_accessDenied_returns403() throws Exception {
            willThrow(new AccessDeniedException("해당 가게에 대한 권한이 없습니다"))
                    .given(storeService).deleteStore(eq(NON_PENDING_STORE_ID), any());

            mockMvc.perform(delete("/api/v1/stores/{storeId}", NON_PENDING_STORE_ID))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.details").exists());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 가게 삭제 시 404 반환")
        void deleteStore_notFound_returns404() throws Exception {
            UUID notExistId = UUID.randomUUID();
            willThrow(new IllegalArgumentException("삭제할 가게가 없습니다."))
                    .given(storeService).deleteStore(eq(notExistId), any());

            mockMvc.perform(delete("/api/v1/stores/{storeId}", notExistId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.details").exists());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/stores/{storeId}/admin - 관리자 상태 변경 (PENDING → CLOSED)")
    class AdminUpdateStatus {

        @Test
        @DisplayName("성공: PENDING 가게를 CLOSED로 변경하면 200 반환")
        void adminApprove_returns200() throws Exception {
            StoreResponse response = StoreResponse.builder()
                    .id(PENDING_STORE_ID)
                    .status(Status.CLOSED)
                    .images(List.of())
                    .build();

            given(storeService.modifyStoreStatusToClose(PENDING_STORE_ID)).willReturn(response);

            mockMvc.perform(patch("/api/v1/stores/{storeId}/admin", PENDING_STORE_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.id").value(PENDING_STORE_ID.toString()))
                    .andExpect(jsonPath("$.data.status").value("CLOSED"));
        }

        @Test
        @DisplayName("실패: PENDING이 아닌 가게 변경 시도하면 409 반환")
        void adminApprove_notPending_returns409() throws Exception {
            given(storeService.modifyStoreStatusToClose(NON_PENDING_STORE_ID))
                    .willThrow(new IllegalStateException("가게가 승인대기 상태가 아닙니다."));

            mockMvc.perform(patch("/api/v1/stores/{storeId}/admin", NON_PENDING_STORE_ID))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("CONFLICT"))
                    .andExpect(jsonPath("$.details").exists());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 가게 상태 변경 시 404 반환")
        void adminApprove_notFound_returns404() throws Exception {
            UUID notExistId = UUID.randomUUID();
            given(storeService.modifyStoreStatusToClose(notExistId))
                    .willThrow(new IllegalArgumentException("해당 가게를 찾을 수 없습니다."));

            mockMvc.perform(patch("/api/v1/stores/{storeId}/admin", notExistId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.details").exists());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/stores/{storeId}/owner - 점주 상태 변경")
    class OwnerUpdateStatus {

        @Test
        @DisplayName("성공: CLOSED → OPENED 변경 시 200 반환")
        void ownerUpdateStatus_returns200() throws Exception {
            StoreStatusUpdateRequest request = new StoreStatusUpdateRequest(Status.OPENED);

            StoreResponse response = StoreResponse.builder()
                    .id(NON_PENDING_STORE_ID)
                    .status(Status.OPENED)
                    .images(List.of())
                    .build();

            given(storeService.modifyStoreStatus(any(), eq(NON_PENDING_STORE_ID), any()))
                    .willReturn(response);

            mockMvc.perform(patch("/api/v1/stores/{storeId}/owner", NON_PENDING_STORE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.id").value(NON_PENDING_STORE_ID.toString()))
                    .andExpect(jsonPath("$.data.status").value("OPENED"));
        }

        @Test
        @DisplayName("실패: PENDING 상태로 변경 시도하면 404 반환")
        void ownerUpdateStatus_toPending_returns404() throws Exception {
            StoreStatusUpdateRequest request = new StoreStatusUpdateRequest(Status.PENDING);

            given(storeService.modifyStoreStatus(any(), eq(NON_PENDING_STORE_ID), any()))
                    .willThrow(new IllegalArgumentException("승인 대기 상태로는 변경할 수 없습니다."));

            mockMvc.perform(patch("/api/v1/stores/{storeId}/owner", NON_PENDING_STORE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.details").exists());
        }

        @Test
        @DisplayName("실패: PENDING 가게의 상태 변경 시도하면 409 반환")
        void ownerUpdateStatus_pendingStore_returns409() throws Exception {
            StoreStatusUpdateRequest request = new StoreStatusUpdateRequest(Status.OPENED);

            given(storeService.modifyStoreStatus(any(), eq(PENDING_STORE_ID), any()))
                    .willThrow(new IllegalStateException("승인 대기 중인 가게의 상태는 변경 불가합니다."));

            mockMvc.perform(patch("/api/v1/stores/{storeId}/owner", PENDING_STORE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("CONFLICT"))
                    .andExpect(jsonPath("$.details").exists());
        }

        @Test
        @DisplayName("실패: 권한 없는 사용자가 상태 변경 시도하면 403 반환")
        void ownerUpdateStatus_accessDenied_returns403() throws Exception {
            StoreStatusUpdateRequest request = new StoreStatusUpdateRequest(Status.OPENED);

            given(storeService.modifyStoreStatus(any(), eq(NON_PENDING_STORE_ID), any()))
                    .willThrow(new AccessDeniedException("해당 가게에 대한 권한이 없습니다"));

            mockMvc.perform(patch("/api/v1/stores/{storeId}/owner", NON_PENDING_STORE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.details").exists());
        }
    }
}
