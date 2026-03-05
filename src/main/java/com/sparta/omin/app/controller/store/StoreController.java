package com.sparta.omin.app.controller.store;

import com.sparta.omin.app.model.store.dto.StoreCreateRequest;
import com.sparta.omin.app.model.store.dto.StoreResponse;
import com.sparta.omin.app.model.store.dto.StoreStatusUpdateRequest;
import com.sparta.omin.app.model.store.dto.StoreUpdateRequest;
import com.sparta.omin.app.model.store.service.StoreService;
import com.sparta.omin.common.response.ApiResponse;
import com.sparta.omin.common.response.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StoreController {

    private final StoreService storeService;

    @PostMapping("/stores")
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(
            @Valid @RequestPart("data") StoreCreateRequest storeCreateRequest,
            @RequestPart("images") List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails user) {
        StoreResponse createdStore = storeService.registerStore(storeCreateRequest, images, user);
        return ResponseUtil.created(
                createdStore
        );
    }

    @GetMapping("/stores/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponse>> getStore(@PathVariable UUID storeId) {
        StoreResponse foundStore = storeService.findStore(storeId);
        return ResponseUtil.ok(
                foundStore
        );
    }

    @PutMapping("/stores/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(
            @PathVariable UUID storeId
            , @Valid @RequestPart("data") StoreUpdateRequest storeUpdateRequest
            , @RequestPart("newImages") List<MultipartFile> newImages
            , @AuthenticationPrincipal UserDetails user
    ) {
        StoreResponse updatedStore = storeService.modifyStore(storeId, storeUpdateRequest, newImages, user);
        return ResponseUtil.ok(
                updatedStore
        );
    }

    @DeleteMapping("/stores/{storeId}")
    public ResponseEntity<Void> deleteStore(@PathVariable UUID storeId, @AuthenticationPrincipal UserDetails user) {
        storeService.deleteStore(storeId, user);
        return ResponseUtil.noContent();
    }

    /**
     * 가게 상태를 CLOSE로 변경하는 API (관리자용)
     *
     * @param storeId 가게 ID
     * @return 변경된 가게 정보
     */
    @PatchMapping("/stores/{storeId}/admin")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStoreStateClose(
            @PathVariable UUID storeId) {
        StoreResponse storeResponse = storeService.modifyStoreStatusToClose(storeId);
        return ResponseUtil.ok(
                storeResponse
        );
    }

    /**
     * 가게 상태를 OPEN로 변경하는 API (점주용)
     *
     * @param storeId 가게 ID
     * @return 변경된 가게 정보
     */
    @PatchMapping("/stores/{storeId}/owner")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStoreStateOpen(
            @Valid @RequestBody StoreStatusUpdateRequest storeStatusUpdateRequest,
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UserDetails user) {
        StoreResponse storeResponse = storeService.modifyStoreStatus(storeStatusUpdateRequest, storeId, user);
        return ResponseUtil.ok(
                storeResponse
        );
    }


}
