package com.sparta.omin.app.controller.store;

import com.sparta.omin.app.model.store.dto.*;
import com.sparta.omin.app.model.store.service.StoreService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<StoreResponse> createStore(
            @Valid @RequestPart("data") StoreCreateRequest storeCreateRequest,
            @Size(min = 1, max = 10, message = "파일은 최소 1개에서 최대 10개까지만 업로드 가능합니다.")
            @RequestPart("images") List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails user) {
        StoreResponse createdStore = storeService.registerStore(storeCreateRequest, images, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStore);
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponse> getStore(@PathVariable UUID storeId) {
        StoreResponse foundStore = storeService.findStore(storeId);
        return ResponseEntity.ok(foundStore);
    }

    @PutMapping("/{storeId}")
    public ResponseEntity<StoreResponse> updateStore(
            @PathVariable UUID storeId,
            @Valid @RequestPart("data") StoreUpdateRequest storeUpdateRequest,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,
            @AuthenticationPrincipal UserDetails user
    ) {
        StoreResponse updatedStore = storeService.modifyStore(storeId, storeUpdateRequest, newImages, user);
        return ResponseEntity.ok(updatedStore);
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStore(@PathVariable UUID storeId, @AuthenticationPrincipal UserDetails user) {
        storeService.deleteStore(storeId, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * 매장 리스트 조회
     */
    @GetMapping
    public ResponseEntity<StoreSearchPageResponse> searchStoreList(
            @ModelAttribute StoreSearchRequest storeSearchRequest,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(storeService.searchStoreList(storeSearchRequest, user));
    }

    /**
     * 점주용: 본인 등록 매장 리스트 조회
     */
    @GetMapping("/owner/my")
    public ResponseEntity<List<StoreListResponse>> searchMyStores(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(storeService.findMyStores(user));
    }

    /**
     * 점주용: 가게 영업 상태 변경 (OPENED <-> CLOSED)
     */
    @PatchMapping("/{storeId}/owner")
    public ResponseEntity<StoreResponse> updateStoreStatus(
            @Valid @RequestBody StoreStatusUpdateRequest storeStatusUpdateRequest,
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UserDetails user) {
        StoreResponse storeResponse = storeService.modifyStoreStatus(storeStatusUpdateRequest, storeId, user);
        return ResponseEntity.ok(storeResponse);
    }

    /**
     * 관리자용: PENDING 상태 매장 전체 조회
     */
    @GetMapping("/admin/pending")
    public ResponseEntity<List<StoreListResponse>> searchPendingStores() {
        return ResponseEntity.ok(storeService.findPendingStores());
    }

    /**
     * 관리자용: 승인 대기(PENDING) 매장을 CLOSED 상태로 승인 처리
     */
    @PatchMapping("/{storeId}/admin")
    public ResponseEntity<StoreResponse> approveAndCloseStore(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UserDetails user) {
        StoreResponse storeResponse = storeService.approveAndCloseStore(storeId, user);
        return ResponseEntity.ok(storeResponse);
    }
}
