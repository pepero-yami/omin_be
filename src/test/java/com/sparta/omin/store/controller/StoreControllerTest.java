package com.sparta.omin.store.controller;

import com.sparta.omin.app.model.store.code.Category;
import com.sparta.omin.app.model.store.code.Status;
import com.sparta.omin.app.model.store.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Store:Controller")
@AutoConfigureMockMvc(addFilters = false)
class StoreControllerTest extends StoreControllerHelper {

    @Nested
    @DisplayName("POST /api/v1/stores - 가게 생성")
    class CreateStore {

        @Test
        @DisplayName("성공: 데이터와 이미지 파일을 함께 전송하면 201 반환")
        void createStore_returns201() throws Exception {
            mockUser();
            StoreCreateRequest request = new StoreCreateRequest(
                    Category.KOREAN, "후와후와",
                    "서울특별시 강남구 테헤란로 427", "1층"
            );
            MockMultipartFile dataFile = new MockMultipartFile(
                    "data", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
            );
            MockMultipartFile imageFile = new MockMultipartFile(
                    "images", "store.jpg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes()
            );
            given(storeService.registerStore(any(), any(), any()))
                    .willReturn(buildStoreResponse(PENDING_STORE_ID, "후와후와", Status.PENDING, Category.KOREAN));

            mockMvc.perform(multipart(STORES_BASE_URL)
                            .file(dataFile)
                            .file(imageFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(PENDING_STORE_ID.toString()))
                    .andExpect(jsonPath("$.name").value("후와후와"))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("실패: 필수 데이터 누락 시 400 반환")
        void createStore_validationError_returns400() throws Exception {
            StoreCreateRequest invalidRequest = new StoreCreateRequest(null, "", "", "");
            MockMultipartFile dataFile = new MockMultipartFile(
                    "data", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsString(invalidRequest).getBytes(StandardCharsets.UTF_8)
            );
            MockMultipartFile imageFile = new MockMultipartFile(
                    "images", "store.jpg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes()
            );

            mockMvc.perform(multipart(STORES_BASE_URL)
                            .file(dataFile)
                            .file(imageFile))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.details").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/stores/{storeId} - 가게 단건 조회")
    class GetStore {

        @Test
        @DisplayName("성공: 존재하는 가게 조회 시 200 반환")
        void getStore_returns200() throws Exception {
            given(storeService.findStore(eq(NON_PENDING_STORE_ID), any()))
                    .willReturn(buildStoreResponse(NON_PENDING_STORE_ID, "후와후와", Status.OPENED, Category.KOREAN));

            mockMvc.perform(get(STORE_URL.formatted(NON_PENDING_STORE_ID)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(NON_PENDING_STORE_ID.toString()))
                    .andExpect(jsonPath("$.name").value("후와후와"));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 가게 조회 시 404 반환")
        void getStore_notFound_returns404() throws Exception {
            UUID notExistId = UUID.randomUUID();
            given(storeService.findStore(eq(notExistId), any()))
                    .willThrow(new IllegalArgumentException("해당 가게를 찾을 수 없습니다."));

            mockMvc.perform(get(STORE_URL.formatted(notExistId)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/stores/{storeId} - 가게 수정")
    class UpdateStore {

        @Test
        @DisplayName("성공: 정보 수정 및 이미지 추가 시 200 반환")
        void updateStore_returns200() throws Exception {
            mockUser();
            StoreUpdateRequest request = new StoreUpdateRequest(
                    Category.CHINESE, "수정후와후와",
                    "서울특별시 서초구 강남대로 465", "2층",
                    List.of(new StoreUpdateRequest.StoreImageRequest(null, StoreUpdateRequest.ImageAction.ADD))
            );
            MockMultipartFile dataFile = new MockMultipartFile(
                    "data", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
            );
            MockMultipartFile newImageFile = new MockMultipartFile(
                    "newImages", "new.jpg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes()
            );
            given(storeService.modifyStore(eq(NON_PENDING_STORE_ID), any(), any(), any()))
                    .willReturn(buildStoreResponse(NON_PENDING_STORE_ID, "수정후와후와", Status.OPENED, Category.CHINESE));

            mockMvc.perform(multipart(org.springframework.http.HttpMethod.PUT,
                            STORE_URL.formatted(NON_PENDING_STORE_ID))
                            .file(dataFile)
                            .file(newImageFile))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(NON_PENDING_STORE_ID.toString()))
                    .andExpect(jsonPath("$.name").value("수정후와후와"))
                    .andExpect(jsonPath("$.category").value("CHINESE"));
        }

        @Test
        @DisplayName("실패: 권한 없는 사용자가 수정 시도하면 403 반환")
        void updateStore_accessDenied_returns403() throws Exception {
            mockUser();
            StoreUpdateRequest request = new StoreUpdateRequest(
                    Category.KOREAN, "테스트",
                    "서울특별시 강남구 테헤란로 427", "1층",
                    List.of(new StoreUpdateRequest.StoreImageRequest(null, StoreUpdateRequest.ImageAction.ADD))
            );
            MockMultipartFile dataFile = new MockMultipartFile(
                    "data", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
            );
            MockMultipartFile newImageFile = new MockMultipartFile(
                    "newImages", "img.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]
            );
            given(storeService.modifyStore(eq(NON_PENDING_STORE_ID), any(), any(), any()))
                    .willThrow(new AccessDeniedException("해당 가게에 대한 권한이 없습니다."));

            mockMvc.perform(multipart(org.springframework.http.HttpMethod.PUT,
                            STORE_URL.formatted(NON_PENDING_STORE_ID))
                            .file(dataFile)
                            .file(newImageFile))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("FORBIDDEN"));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 가게 수정 시 404 반환")
        void updateStore_notFound_returns404() throws Exception {
            mockUser();
            UUID notExistId = UUID.randomUUID();
            StoreUpdateRequest request = new StoreUpdateRequest(
                    Category.KOREAN, "없는가게",
                    "서울특별시 강남구 테헤란로 427", "1층",
                    List.of(new StoreUpdateRequest.StoreImageRequest(null, StoreUpdateRequest.ImageAction.ADD))
            );
            MockMultipartFile dataFile = new MockMultipartFile(
                    "data", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
            );
            MockMultipartFile newImageFile = new MockMultipartFile(
                    "newImages", "img.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]
            );
            given(storeService.modifyStore(eq(notExistId), any(), any(), any()))
                    .willThrow(new IllegalArgumentException("해당 가게를 찾을 수 없습니다."));

            mockMvc.perform(multipart(org.springframework.http.HttpMethod.PUT,
                            STORE_URL.formatted(notExistId))
                            .file(dataFile)
                            .file(newImageFile))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/stores/{storeId} - 가게 삭제")
    class DeleteStore {

        @Test
        @DisplayName("성공: 가게 삭제 시 204 반환")
        void deleteStore_returns204() throws Exception {
            mockUser();
            willDoNothing().given(storeService).deleteStore(eq(NON_PENDING_STORE_ID), any());

            mockMvc.perform(delete(STORE_URL.formatted(NON_PENDING_STORE_ID)))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("실패: 권한 없는 사용자가 삭제 시도하면 403 반환")
        void deleteStore_accessDenied_returns403() throws Exception {
            mockUser();
            willThrow(new AccessDeniedException("해당 가게에 대한 권한이 없습니다."))
                    .given(storeService).deleteStore(eq(NON_PENDING_STORE_ID), any());

            mockMvc.perform(delete(STORE_URL.formatted(NON_PENDING_STORE_ID)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("FORBIDDEN"));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 가게 삭제 시 404 반환")
        void deleteStore_notFound_returns404() throws Exception {
            mockUser();
            UUID notExistId = UUID.randomUUID();
            willThrow(new IllegalArgumentException("삭제할 가게가 없습니다."))
                    .given(storeService).deleteStore(eq(notExistId), any());

            mockMvc.perform(delete(STORE_URL.formatted(notExistId)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/stores/owner/my - 점주 본인 매장 조회")
    class GetMyStores {

        @Test
        @DisplayName("성공: 내 매장 목록을 200으로 반환")
        void getMyStores_returns200() throws Exception {
            mockUser();
            StoreListPageResponse pageResponse = new StoreListPageResponse(
                    List.of(
                            buildStoreListResponse(PENDING_STORE_ID, "후와후와본점", Status.PENDING),
                            buildStoreListResponse(NON_PENDING_STORE_ID, "후와후와2호점", Status.OPENED)
                    ), false, 0
            );
            given(storeService.findMyStores(any(), any())).willReturn(pageResponse);

            mockMvc.perform(get(STORE_OWNER_MY_URL))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.hasNext").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/stores/admin/pending - 관리자용 PENDING 매장 조회")
    class GetPendingStores {

        @Test
        @DisplayName("성공: PENDING 매장 목록을 200으로 반환")
        void getPendingStores_returns200() throws Exception {
            StoreListPageResponse pageResponse = new StoreListPageResponse(
                    List.of(buildStoreListResponse(PENDING_STORE_ID, "후와후와본점", Status.PENDING)),
                    false, 0
            );
            given(storeService.findPendingStores(any())).willReturn(pageResponse);

            mockMvc.perform(get(STORE_ADMIN_PENDING_URL))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                    .andExpect(jsonPath("$.hasNext").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/stores - 매장 리스트 검색 (커서 기반)")
    class SearchStores {

        @Test
        @DisplayName("성공: 검색 결과를 200으로 반환")
        void searchStores_returns200() throws Exception {
            mockUser();
            UUID addressId = UUID.randomUUID();
            StoreSearchPageResponse pageResponse = new StoreSearchPageResponse(
                    List.of(new StoreSearchResponse(
                            NON_PENDING_STORE_ID, Category.KOREAN, "후와후와",
                            "서울특별시 강남구 테헤란로 427", "1층", Status.OPENED, "store.jpg"
                    )),
                    false, null, null
            );
            given(storeService.searchStoreList(any(), any())).willReturn(pageResponse);

            mockMvc.perform(get(STORES_BASE_URL)
                            .param("addressId", addressId.toString())
                            .param("category", Category.KOREAN.name()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.hasNext").value(false));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/stores/{storeId}/admin - 관리자 승인 (PENDING → CLOSED)")
    class AdminApproveStore {

        @Test
        @DisplayName("성공: PENDING 가게를 CLOSED로 승인하면 200 반환")
        void adminApprove_returns200() throws Exception {
            mockUser();
            given(storeService.approveAndCloseStore(eq(PENDING_STORE_ID), any()))
                    .willReturn(buildStoreResponse(PENDING_STORE_ID, "후와후와", Status.CLOSED, Category.KOREAN));

            mockMvc.perform(patch(STORE_ADMIN_URL.formatted(PENDING_STORE_ID)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(PENDING_STORE_ID.toString()))
                    .andExpect(jsonPath("$.status").value("CLOSED"));
        }

        @Test
        @DisplayName("실패: PENDING이 아닌 가게 승인 시도하면 409 반환")
        void adminApprove_notPending_returns409() throws Exception {
            mockUser();
            given(storeService.approveAndCloseStore(eq(NON_PENDING_STORE_ID), any()))
                    .willThrow(new IllegalStateException("가게가 승인대기 상태가 아닙니다."));

            mockMvc.perform(patch(STORE_ADMIN_URL.formatted(NON_PENDING_STORE_ID)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("CONFLICT"));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 가게 승인 시 404 반환")
        void adminApprove_notFound_returns404() throws Exception {
            mockUser();
            UUID notExistId = UUID.randomUUID();
            given(storeService.approveAndCloseStore(eq(notExistId), any()))
                    .willThrow(new IllegalArgumentException("해당 가게를 찾을 수 없습니다."));

            mockMvc.perform(patch(STORE_ADMIN_URL.formatted(notExistId)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/stores/{storeId}/owner - 점주 영업 상태 변경")
    class OwnerUpdateStatus {

        @Test
        @DisplayName("성공: CLOSED → OPENED 변경 시 200 반환")
        void ownerUpdateStatus_returns200() throws Exception {
            mockUser();
            StoreStatusUpdateRequest request = new StoreStatusUpdateRequest(Status.OPENED);
            given(storeService.modifyStoreStatus(any(), eq(NON_PENDING_STORE_ID), any()))
                    .willReturn(buildStoreResponse(NON_PENDING_STORE_ID, "후와후와", Status.OPENED, Category.KOREAN));

            mockMvc.perform(patch(STORE_OWNER_URL.formatted(NON_PENDING_STORE_ID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(NON_PENDING_STORE_ID.toString()))
                    .andExpect(jsonPath("$.status").value("OPENED"));
        }

        @Test
        @DisplayName("실패: PENDING 상태로 변경 시도하면 404 반환")
        void ownerUpdateStatus_toPending_returns404() throws Exception {
            mockUser();
            StoreStatusUpdateRequest request = new StoreStatusUpdateRequest(Status.PENDING);
            given(storeService.modifyStoreStatus(any(), eq(NON_PENDING_STORE_ID), any()))
                    .willThrow(new IllegalArgumentException("승인 대기 상태로는 변경할 수 없습니다."));

            mockMvc.perform(patch(STORE_OWNER_URL.formatted(NON_PENDING_STORE_ID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("NOT_FOUND"));
        }

        @Test
        @DisplayName("실패: PENDING 가게의 상태 변경 시도하면 409 반환")
        void ownerUpdateStatus_pendingStore_returns409() throws Exception {
            mockUser();
            StoreStatusUpdateRequest request = new StoreStatusUpdateRequest(Status.OPENED);
            given(storeService.modifyStoreStatus(any(), eq(PENDING_STORE_ID), any()))
                    .willThrow(new IllegalStateException("승인 대기 중인 가게의 상태는 변경 불가합니다."));

            mockMvc.perform(patch(STORE_OWNER_URL.formatted(PENDING_STORE_ID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("CONFLICT"));
        }

        @Test
        @DisplayName("실패: 권한 없는 사용자가 상태 변경 시도하면 403 반환")
        void ownerUpdateStatus_accessDenied_returns403() throws Exception {
            mockUser();
            StoreStatusUpdateRequest request = new StoreStatusUpdateRequest(Status.OPENED);
            given(storeService.modifyStoreStatus(any(), eq(NON_PENDING_STORE_ID), any()))
                    .willThrow(new AccessDeniedException("해당 가게에 대한 권한이 없습니다."));

            mockMvc.perform(patch(STORE_OWNER_URL.formatted(NON_PENDING_STORE_ID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("FORBIDDEN"));
        }
    }
}
