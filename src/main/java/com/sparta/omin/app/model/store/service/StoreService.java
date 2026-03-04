package com.sparta.omin.app.model.store.service;

import com.sparta.omin.app.model.store.dto.StoreCreateRequest;
import com.sparta.omin.app.model.store.dto.StoreResponse;
import com.sparta.omin.app.model.store.dto.StoreUpdateRequest;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.store.entity.StoreImage;
import com.sparta.omin.app.model.store.repos.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
    public StoreResponse findStore(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 점포를 찾을 수 없습니다."));
        return StoreResponse.of(store);
    }

    @Transactional
    public StoreResponse modifyStore(StoreUpdateRequest storeUpdateRequest, UUID storeId) {
        Store savedStore = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 점포를 찾을 수 없습니다."));
        savedStore.updateStore(storeUpdateRequest);
        //이미지 삭제요청 처리
        List<StoreUpdateRequest.StoreImageRequest> imageRequests = storeUpdateRequest.images();
        handleDeleteImgRequest(savedStore, imageRequests);
        //신규 이미지 등록 및 재정렬
        registerAndSortImgs(savedStore, imageRequests);

        return StoreResponse.of(savedStore);
    }

    @Transactional
    public void deleteStore(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 가게가 없습니다."));
        storeRepository.delete(store); // entity의 update 쿼리가 대신 실행
    }

    private static void handleDeleteImgRequest(
            Store savedStore,
            List<StoreUpdateRequest.StoreImageRequest> imageRequests
    ) {
        //request에 포함된 기존 이미지 id 수집
        Set<UUID> requestImageIds = imageRequests.stream()
                .map(StoreUpdateRequest.StoreImageRequest::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        //request에 없는 기존 이미지 삭제 (orphanRemoval 동작)
        savedStore.getImages().removeIf(img -> !requestImageIds.contains(img.getId()));
    }

    private static void registerAndSortImgs(Store savedStore, List<StoreUpdateRequest.StoreImageRequest> imageRequests) {
        //기존 이미지를 id로 빠르게 찾기 위한 Map
        Map<UUID, StoreImage> existingImageMap = savedStore.getImages().stream()
                .collect(Collectors.toMap(StoreImage::getId, Function.identity()));

        //request 순서대로 순번 반영 + 신규 추가
        for (int i = 0; i < imageRequests.size(); i++) {
            StoreUpdateRequest.StoreImageRequest req = imageRequests.get(i);
            int newSequence = i + 1;

            if (req.id() != null) {
                // 기존 DB에 저장되어 있던 이미지 → 순번 변동사항만 갱신
                StoreImage existing = existingImageMap.get(req.id());
                existing.setSequence(newSequence);
            } else {
                // 신규 이미지 → 생성 후 추가
                StoreImage newImage = new StoreImage(req.url());
                newImage.setStore(savedStore);
                savedStore.getImages().add(newImage);
                newImage.setSequence(newSequence);
            }
        }
    }

}
