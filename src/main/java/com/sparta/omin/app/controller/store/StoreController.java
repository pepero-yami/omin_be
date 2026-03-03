package com.sparta.omin.app.controller.store;

import com.sparta.omin.app.model.store.dto.StoreCreateRequest;
import com.sparta.omin.app.model.store.dto.StoreResponse;
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

}
