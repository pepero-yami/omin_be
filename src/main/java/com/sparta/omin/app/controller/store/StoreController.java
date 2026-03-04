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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StoreController {

    private final StoreService storeService;

    @PostMapping("/stores")
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(@Valid @RequestBody StoreCreateRequest storeCreateRequest) {
       StoreResponse createdStore = storeService.registerStore(storeCreateRequest);
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
            @Valid @RequestBody StoreUpdateRequest storeUpdateRequest ,@PathVariable UUID storeId
    ){
        StoreResponse updatedStore = storeService.modifyStore(storeUpdateRequest, storeId);
        return ResponseUtil.ok(
                updatedStore
        );
    }

    @DeleteMapping("/stores/{storeId}")
    public ResponseEntity<Void> deleteStore(@PathVariable UUID storeId) {
        storeService.deleteStore(storeId);
        return ResponseUtil.noContent();
    }

    /**
     * 가게 상태를 CLOSE로 변경하는 API (관리자용)
     * @param storeId 가게 ID
     * @return 변경된 가게 정보
     */
    @PatchMapping("/stores/{storeId}/admin")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStoreStateClose(@PathVariable UUID storeId){
        StoreResponse storeResponse = storeService.modifyStoreStatusToClose(storeId);
        return ResponseUtil.ok(
            storeResponse
        );
    }

    /**
     * 가게 상태를 OPEN로 변경하는 API (점주용)
     * @param storeId 가게 ID
     * @return 변경된 가게 정보
     */
    @PatchMapping("/stores/{storeId}/owner")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStoreStateOpen(
            @Valid @RequestBody StoreStatusUpdateRequest storeStatusUpdateRequest, @PathVariable UUID storeId){
        StoreResponse storeResponse = storeService.modifyStoreStatus(storeStatusUpdateRequest, storeId);
        return ResponseUtil.ok(
                storeResponse
        );
    }


}
