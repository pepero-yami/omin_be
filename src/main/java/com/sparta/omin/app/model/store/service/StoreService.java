package com.sparta.omin.app.model.store.service;

import com.sparta.omin.app.model.store.dto.StoreCreateRequest;
import com.sparta.omin.app.model.store.dto.StoreResponse;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.store.entity.StoreImage;
import com.sparta.omin.app.model.store.repos.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    @Transactional
    public StoreResponse registerStore(StoreCreateRequest storeCreateRequest) {
        Store store = storeCreateRequest.toEntity();
        for (String imageUrl : storeCreateRequest.images()) {
            StoreImage storeImage = new StoreImage(imageUrl);
            store.addImage(storeImage);
        }
        Store savedStore = storeRepository.save(store);
        return StoreResponse.of(savedStore);
    }

    //조회
    @Transactional(readOnly = true)
    public StoreResponse findStore(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store 없음."));
        return StoreResponse.of(store);
    }
}
