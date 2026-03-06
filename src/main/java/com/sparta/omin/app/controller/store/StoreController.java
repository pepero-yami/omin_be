package com.sparta.omin.app.controller.store;

import com.sparta.omin.app.model.store.dto.StoreCreateRequest;
import com.sparta.omin.app.model.store.dto.StoreResponse;
import com.sparta.omin.app.model.store.dto.StoreStatusUpdateRequest;
import com.sparta.omin.app.model.store.dto.StoreUpdateRequest;
import com.sparta.omin.app.model.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<StoreResponse> createStore(
            @Valid @RequestPart("data") StoreCreateRequest storeCreateRequest,
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
            @PathVariable UUID storeId
            , @Valid @RequestPart("data") StoreUpdateRequest storeUpdateRequest
            , @RequestPart("newImages") List<MultipartFile> newImages
            , @AuthenticationPrincipal UserDetails user
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
     * 가게 상태를 CLOSE로 변경하는 API (관리자용)
     *
     * @param storeId 가게 ID
     * @return 변경된 가게 정보
     */
    @PatchMapping("/{storeId}/admin")
    public ResponseEntity<StoreResponse> updateStoreStateClose(
            @PathVariable UUID storeId) {
        StoreResponse storeResponse = storeService.modifyStoreStatusToClose(storeId);
        return ResponseEntity.ok(storeResponse);
    }

    /**
     * 가게 상태를 OPEN로 변경하는 API (점주용)
     *
     * @param storeId 가게 ID
     * @return 변경된 가게 정보
     */
    @PatchMapping("/{storeId}/owner")
    public ResponseEntity<StoreResponse> updateStoreStateOpen(
            @Valid @RequestBody StoreStatusUpdateRequest storeStatusUpdateRequest,
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UserDetails user) {
        StoreResponse storeResponse = storeService.modifyStoreStatus(storeStatusUpdateRequest, storeId, user);
        return ResponseEntity.ok(storeResponse);
    }


}
