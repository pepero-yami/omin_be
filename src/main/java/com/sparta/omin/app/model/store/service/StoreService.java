package com.sparta.omin.app.model.store.service;

import com.sparta.omin.app.model.store.dto.StoreCreateRequest;
import com.sparta.omin.app.model.store.dto.StoreResponse;
import com.sparta.omin.app.model.store.dto.StoreStatusUpdateRequest;
import com.sparta.omin.app.model.store.dto.StoreUpdateRequest;
import com.sparta.omin.app.model.store.code.Status;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.store.entity.StoreImage;
import com.sparta.omin.app.model.store.repos.StoreRepository;
import com.sparta.omin.app.model.user.constants.Role;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;

    @Transactional
    public StoreResponse registerStore(StoreCreateRequest storeCreateRequest, List<MultipartFile> images, UserDetails user) {
        User loginUser = (User) user;
        Store store = storeCreateRequest.toEntity(loginUser.getId());
        //s3 전송 후 받아온 이미지 url
        List<String> imageUrlList = sendImagesToS3(images);

        for (String imageUrl : imageUrlList) {
            StoreImage storeImage = new StoreImage(imageUrl);
            store.addImage(storeImage);
        }
        Store savedStore = storeRepository.save(store);
        return StoreResponse.of(savedStore);
    }

    //단건조회
    public StoreResponse findStore(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ApiException(ErrorCode.STORE_NOT_FOUND));
        return StoreResponse.of(store);
    }

    @Transactional
    public StoreResponse modifyStore(UUID storeId, StoreUpdateRequest storeUpdateRequest, List<MultipartFile> newImages, UserDetails user) {
        Store savedStore = storeRepository.findById(storeId)
                .orElseThrow(() -> new ApiException(ErrorCode.STORE_NOT_FOUND));
        hasStoreAuth(user, savedStore);
        savedStore.updateStore(storeUpdateRequest.regionId(), storeUpdateRequest.category(), storeUpdateRequest.name()
                , storeUpdateRequest.roadAddress(), storeUpdateRequest.detailAddress(), storeUpdateRequest.latitude()
                , storeUpdateRequest.longitude());

        //이미지 삭제요청 처리
        List<StoreUpdateRequest.StoreImageRequest> imageRequests = storeUpdateRequest.images();
        handleDeleteImgRequest(savedStore, imageRequests);
        //신규 이미지 등록 및 재정렬
        List<String> newUrlList = sendImagesToS3(newImages);
        registerAndSortImgs(savedStore, imageRequests, newUrlList);

        return StoreResponse.of(savedStore);
    }

    @Transactional
    public void deleteStore(UUID storeId, UserDetails user) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ApiException(ErrorCode.STORE_NOT_FOUND));
        hasStoreAuth(user, store);
        storeRepository.delete(store); // entity의 update 쿼리가 대신 실행
    }

    //점포 승인대기 -> 승인완료 상태(PENDING)->(CLOSE)
    @Transactional
    public StoreResponse modifyStoreStatusToClose(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ApiException(ErrorCode.STORE_NOT_FOUND));
        if (store.getStatus() != Status.PENDING) {
            throw new ApiException(ErrorCode.STORE_STATUS_NOT_PENDING);
        }
        store.updateStatus(Status.CLOSED);
        return StoreResponse.of(store);
    }

    //점포 상태 (CLOSED) -> (OPENED)
    @Transactional
    public StoreResponse modifyStoreStatus(StoreStatusUpdateRequest storeStatusUpdateRequest, UUID storeId, UserDetails user) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ApiException(ErrorCode.STORE_NOT_FOUND));
        hasStoreAuth(user, store);
        if (storeStatusUpdateRequest.status() == Status.PENDING) {
            throw new ApiException(ErrorCode.STORE_STATUS_INVALID_CHANGE);
        }
        //가게 상태가 PENDING 일 때 예외발생
        if (store.getStatus() == Status.PENDING) {
            throw new ApiException(ErrorCode.STORE_STATUS_PENDING_CANNOT_MODIFY);
        }
        store.updateStatus(storeStatusUpdateRequest.status());
        return StoreResponse.of(store);
    }

    private static void hasStoreAuth(UserDetails user, Store store) {
        User loginUser = (User) user;

        //관리자면 통과
        if (loginUser.getRole() == Role.MANAGER || loginUser.getRole() == Role.MASTER) {
            return;
        }
        //관리자가 아니라면 반드시 가게 주인이어야 함
        if (!store.getOwnerId().equals(loginUser.getId())) {
            throw new ApiException(ErrorCode.STORE_ACCESS_DENIED);
        }
    }

    //임시코드 : s3연동 되면 변경 예정.
    private static List<String> sendImagesToS3(List<MultipartFile> images) {
        List<String> imagesList = new ArrayList<>();
        for (MultipartFile file : images) {
            imagesList.add(file.getOriginalFilename());
        }
        return imagesList;
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

    private static void registerAndSortImgs(Store savedStore, List<StoreUpdateRequest.StoreImageRequest> imageRequests, List<String> newUrlList) {
        //기존 이미지를 id로 빠르게 찾기 위한 Map
        Map<UUID, StoreImage> existingImageMap = savedStore.getImages().stream()
                .collect(Collectors.toMap(StoreImage::getId, Function.identity()));
        //request 순서대로 순번 반영 + 신규 추가
        int currentNewImageSequence = 0;
        for (int i = 0; i < imageRequests.size(); i++) {
            StoreUpdateRequest.StoreImageRequest req = imageRequests.get(i);
            int newSequence = i + 1;

            if (!req.isNewUploaded()) {
                // 기존 DB에 저장되어 있던 이미지 → 순번 변동사항만 갱신
                StoreImage existing = existingImageMap.get(req.id());
                existing.updateImageSorting(newSequence);
            } else {
                // 신규 이미지 → 생성 후 추가
                StoreImage newImage = new StoreImage(newUrlList.get(currentNewImageSequence++));
                savedStore.getImages().add(newImage);
                newImage.mappingNewStoreImage(newSequence,savedStore);
            }
        }
    }

}
