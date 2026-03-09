package com.sparta.omin.app.model.store.service;

import com.sparta.omin.app.model.store.dto.request.StoreCreateRequest;
import com.sparta.omin.app.model.store.dto.request.StoreUpdateRequest;
import com.sparta.omin.app.model.store.dto.response.StoreResponse;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.store.entity.StoreImage;
import com.sparta.omin.app.model.store.repos.StoreRepository;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreWriter {

    private final StoreRepository storeRepository;

    private static final int MAX_IMAGE_COUNT = 10;

    //db 저장만
    @Transactional
    public StoreResponse save(StoreCreateRequest request, UUID ownerId, Point coordinates, List<String> imageUrlList) {
        Store store = Store.builder()
                .ownerId(ownerId)
                .category(request.category())
                .name(request.name())
                .roadAddress(request.roadAddress())
                .detailAddress(request.detailAddress())
                .coordinates(coordinates)
                .build();
        for (String imageUrl : imageUrlList) {
            store.addImage(new StoreImage(imageUrl));
        }
        Store savedStore = storeRepository.save(store);
        log.debug("매장 등록 완료 - storeId: {}", savedStore.getId());
        return StoreResponse.of(savedStore);
    }

    @Transactional
    public StoreResponse update(UUID storeId, StoreUpdateRequest request, Point coordinates, List<String> newUrlList) {
        Store store = storeRepository.findByIdWithImages(storeId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.STORE_NOT_FOUND));
        store.updateStore(request.category(), request.name(), request.roadAddress(), request.detailAddress(), coordinates);
        processImages(store, request.images(), newUrlList);
        log.info("매장 수정 완료 - storeId: {}", storeId);
        return StoreResponse.of(store);
    }

    private static void processImages(Store store, List<StoreUpdateRequest.StoreImageRequest> imageRequests, List<String> newUrlList) {
        // action 유효성 검증: ADD는 id 없어야 하고, KEEP/DELETE는 id 필수 + 중복 id 금지
        Set<UUID> seenIds = new HashSet<>();
        for (StoreUpdateRequest.StoreImageRequest req : imageRequests) {
            boolean isAdd = req.action() == StoreUpdateRequest.ImageAction.ADD;
            //add 인경우 id null이여야함
            if (isAdd && req.id() != null) {
                throw new OminBusinessException(ErrorCode.STORE_IMAGE_INVALID_ACTION);
            }
            //keep delete인 경우 id는 null이 아니여야함
            if (!isAdd && req.id() == null) {
                throw new OminBusinessException(ErrorCode.STORE_IMAGE_INVALID_ACTION);
            }
            //keep delete인 경우 아이디가 중복이 아니여야함.
            if (!isAdd && !seenIds.add(req.id())) {
                throw new OminBusinessException(ErrorCode.STORE_IMAGE_DUPLICATE_ID);
            }
        }

        // 최종 이미지 수 검증: KEEP + ADD 만 실제 이미지로 남음
        long finalImageCount = imageRequests.stream()
                .filter(r -> r.action() == StoreUpdateRequest.ImageAction.KEEP
                          || r.action() == StoreUpdateRequest.ImageAction.ADD)
                .count();
        if (finalImageCount < 1) {
            throw new OminBusinessException(ErrorCode.STORE_IMAGE_MIN_REQUIRED);
        }
        if (finalImageCount > MAX_IMAGE_COUNT) {
            throw new OminBusinessException(ErrorCode.STORE_IMAGE_MAX_EXCEEDED);
        }

        // DELETE 처리
        Set<UUID> deleteIds = imageRequests.stream()
                .filter(r -> r.action() == StoreUpdateRequest.ImageAction.DELETE)
                .map(StoreUpdateRequest.StoreImageRequest::id)
                .collect(Collectors.toSet());
        store.removeImagesIn(deleteIds);

        // KEEP/ADD 순서대로 sequence 부여
        Map<UUID, StoreImage> existingImageMap = store.getImages().stream()
                .collect(Collectors.toMap(StoreImage::getId, Function.identity()));

        int addIdx = 0;
        int sequence = 1;
        for (StoreUpdateRequest.StoreImageRequest req : imageRequests) {
            switch (req.action()) {
                case KEEP -> {
                    StoreImage img = existingImageMap.get(req.id());
                    if (img == null) {
                        throw new OminBusinessException(ErrorCode.STORE_IMAGE_NOT_FOUND);
                    }
                    img.updateImageSorting(sequence++);
                }
                case ADD -> store.addNewImage(newUrlList.get(addIdx++), sequence++);
            }
        }
    }
}
