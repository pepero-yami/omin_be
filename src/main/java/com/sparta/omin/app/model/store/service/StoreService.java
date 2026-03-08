package com.sparta.omin.app.model.store.service;

import com.sparta.omin.app.model.address.dto.CoordinatesSearchDto;
import com.sparta.omin.app.model.address.service.CoordinatesSearchService;
import com.sparta.omin.app.model.region.client.KakaoAddressClient;
import com.sparta.omin.app.model.store.code.Status;
import com.sparta.omin.app.model.store.dto.*;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.store.entity.StoreImage;
import com.sparta.omin.app.model.store.repos.StoreRepository;
import com.sparta.omin.app.model.user.constants.Role;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.app.model.user.service.UserPromoteService;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final CoordinatesSearchService coordinatesSearchService;
    private final UserPromoteService userPromoteService;
    private final KakaoAddressClient kakaoAddressClient;

    //point로 변환
    //4326 : 좌표 표준 코드
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public StoreResponse registerStore(StoreCreateRequest storeCreateRequest, List<MultipartFile> images, UserDetails user) {
        User loginUser = (User) user;
        log.debug("매장 등록 요청 - ownerId: {}, name: {}", loginUser.getId(), storeCreateRequest.name());

        //동일한 도로명 주소 + 상세 주소 조합이 이미 DB에 있으면
        if (storeRepository.existsByRoadAddressAndDetailAddress(storeCreateRequest.roadAddress(), storeCreateRequest.detailAddress())) {
            throw new OminBusinessException(ErrorCode.STORE_DUPLICATE_ADDRESS);
        }

        //kakao API
        KakaoAddressClient.KakaoAddressResult kakao = kakaoAddressClient.searchAddress(storeCreateRequest.roadAddress());
        Point coordinates = toPoint(kakao);

        Store store = toEntity(storeCreateRequest, loginUser.getId(), coordinates);

        List<String> imageUrlList = sendImagesToS3(images);
        for (String imageUrl : imageUrlList) {
            store.addImage(new StoreImage(imageUrl));
        }
        Store savedStore = storeRepository.save(store);
        log.debug("매장 등록 완료 - storeId: {}", savedStore.getId());
        return StoreResponse.of(savedStore);
    }

    public StoreResponse findStore(UUID storeId, UserDetails user) {
        log.debug("매장 단건 조회 - storeId: {}", storeId);
        Store store = storeRepository.findByIdWithImages(storeId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.STORE_NOT_FOUND));
        if (store.getStatus() == Status.PENDING) {
            User loginUser = (User) user;
            boolean isAdmin = loginUser.getRole() == Role.MANAGER || loginUser.getRole() == Role.MASTER;
            boolean isOwner = store.getOwnerId().equals(loginUser.getId());
            if (!isAdmin && !isOwner) {
                throw new OminBusinessException(ErrorCode.STORE_NOT_FOUND);
            }
        }
        return StoreResponse.of(store);
    }

    @Transactional
    public StoreResponse modifyStore(UUID storeId, StoreUpdateRequest storeUpdateRequest, List<MultipartFile> newImages, UserDetails user) {
        log.info("매장 수정 요청 - storeId: {}", storeId);
        Store savedStore = storeRepository.findByIdWithImages(storeId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.STORE_NOT_FOUND));
        hasStoreAuth(user, savedStore);
        //다른매장의 동일한 주소로 수정 불가
        if (storeRepository.existsByRoadAddressAndDetailAddressAndIdNot(storeUpdateRequest.roadAddress(), storeUpdateRequest.detailAddress(), storeId)) {
            throw new OminBusinessException(ErrorCode.STORE_DUPLICATE_ADDRESS);
        }
        boolean addressChanged = !savedStore.getRoadAddress().equals(storeUpdateRequest.roadAddress());
        Point coordinates = addressChanged
                ? toPoint(kakaoAddressClient.searchAddress(storeUpdateRequest.roadAddress()))
                : savedStore.getCoordinates();
        log.debug("주소 변경 여부 - changed: {}", addressChanged);
        savedStore.updateStore(storeUpdateRequest.category(), storeUpdateRequest.name(),
                storeUpdateRequest.roadAddress(), storeUpdateRequest.detailAddress(), coordinates);

        List<StoreUpdateRequest.StoreImageRequest> imageRequests = storeUpdateRequest.images();
        List<MultipartFile> imageFiles = newImages != null ? newImages : List.of();
        //ADD 항목 수와 업로드 파일 수 일치 검증
        long addCount = imageRequests.stream()
                .filter(r -> r.action() == StoreUpdateRequest.ImageAction.ADD)
                .count();
        if (addCount != imageFiles.size()) {
            throw new OminBusinessException(ErrorCode.STORE_IMAGE_COUNT_MISMATCH);
        }
        List<String> newUrlList = sendImagesToS3(imageFiles);
        processImages(savedStore, imageRequests, newUrlList);

        log.info("매장 수정 완료 - storeId: {}", storeId);
        return StoreResponse.of(savedStore);
    }

    @Transactional
    public void deleteStore(UUID storeId, UserDetails user) {
        log.info("매장 삭제 요청 - storeId: {}", storeId);
        Store store = storeRepository.findByIdWithImages(storeId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.STORE_NOT_FOUND));
        hasStoreAuth(user, store);
        storeRepository.delete(store);
        log.info("매장 삭제 완료 - storeId: {}", storeId);
    }

    //customer용 매장 조회 리스트
    public StoreSearchPageResponse searchStoreList(StoreSearchRequest storeSearchRequest, UserDetails user) {
        UUID userId = ((User) user).getId();
        //배송지 좌표조회
        CoordinatesSearchDto addressCoordinate = coordinatesSearchService.getCoordinates(storeSearchRequest.addressId(), userId);
        //반경 5km 내
        final double radius = 5000;
        Point center = toPoint(addressCoordinate);

        log.info("목록 검색 - userId: {}, Point: {}, Radius: {}, Category: {}, Name: {}, LastDist: {}, LastId: {}, Size: {}",
                userId, center, radius, storeSearchRequest.category(),
                storeSearchRequest.name(), storeSearchRequest.lastDistance(),
                storeSearchRequest.lastId(), storeSearchRequest.size());

        Slice<StoreRepository.StoreSearchProjection> result = storeRepository.findByCenterAndRadiusOrderByDistance(
                center, radius,
                storeSearchRequest.category(),
                storeSearchRequest.name(),
                storeSearchRequest.lastDistance(),
                storeSearchRequest.lastId(),
                PageRequest.of(0, storeSearchRequest.size())
        );

        log.debug("목록 검색 결과 - count: {}, hasNext: {}", result.getContent().size(), result.hasNext());

        List<StoreSearchResponse> content = result.getContent().stream()
                .map(projection -> new StoreSearchResponse(
                        projection.getStoreId(), projection.getCategory(), projection.getName(),
                        projection.getRoadAddress(), projection.getDetailAddress(), projection.getStatus(), projection.getMainImage()
                ))
                .toList();

        //페이징처리
        Double nextLastDistance = null;
        UUID nextLastId = null;
        if (result.hasNext() && !result.getContent().isEmpty()) {
            StoreRepository.StoreSearchProjection lastProjection = result.getContent().get(result.getContent().size() - 1);
            nextLastDistance = lastProjection.getDistance();
            nextLastId = lastProjection.getStoreId();
        }
        return new StoreSearchPageResponse(content, result.hasNext(), nextLastDistance, nextLastId);
    }

    // 점주용: 본인 등록 매장 목록 조회
    public StoreListPageResponse findMyStores(StoreListPageRequest pageRequest, UserDetails user) {
        UUID ownerId = ((User) user).getId();
        log.debug("본인 매장 목록 조회 - ownerId: {}, page: {}, size: {}", ownerId, pageRequest.page(), pageRequest.size());
        Slice<Store> result = storeRepository.findByOwnerIdOrderByCreatedAtDesc(
                ownerId, PageRequest.of(pageRequest.page(), pageRequest.size()));
        return toPageResponse(result, pageRequest.page());
    }

    // 관리자용: PENDING 상태 매장 목록 조회
    public StoreListPageResponse findPendingStores(StoreListPageRequest pageRequest) {
        log.debug("PENDING 매장 목록 조회 - page: {}, size: {}", pageRequest.page(), pageRequest.size());
        Slice<Store> result = storeRepository.findByStatusOrderByCreatedAtDesc(
                Status.PENDING, PageRequest.of(pageRequest.page(), pageRequest.size()));
        return toPageResponse(result, pageRequest.page());
    }

    private static StoreListPageResponse toPageResponse(Slice<Store> slice, int page) {
        List<StoreListResponse> content = slice.getContent().stream()
                .map(StoreListResponse::of)
                .toList();
        return new StoreListPageResponse(content, slice.hasNext(), page);
    }

    // 관리자용: 승인 대기(PENDING) 매장을 CLOSED 상태로 승인 처리
    @Transactional
    public StoreResponse approveAndCloseStore(UUID storeId, UserDetails user) {
        User loginUser = (User) user;
        log.info("매장 승인 요청 - storeId: {}, adminId: {}", storeId, loginUser.getId());
        if (loginUser.getRole() != Role.MANAGER && loginUser.getRole() != Role.MASTER) {
            throw new OminBusinessException(ErrorCode.STORE_ACCESS_DENIED);
        }
        Store store = storeRepository.findByIdWithImages(storeId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.STORE_NOT_FOUND));
        if (store.getStatus() != Status.PENDING) {
            throw new OminBusinessException(ErrorCode.STORE_STATUS_NOT_PENDING);
        }
        //pending -> closed로 변경
        store.updateStatus(Status.CLOSED);
        //customer -> owner로 변경
        userPromoteService.promoteToOwnerIfCustomer(store.getOwnerId());
        log.info("매장 승인 완료 - storeId: {}, status: PENDING -> CLOSED", storeId);
        return StoreResponse.of(store);
    }

    //점포 상태 (CLOSED) -> (OPENED)
    @Transactional
    public StoreResponse modifyStoreStatus(StoreStatusUpdateRequest storeStatusUpdateRequest, UUID storeId, UserDetails user) {
        log.info("매장 상태 변경 요청 - storeId: {}, targetStatus: {}", storeId, storeStatusUpdateRequest.status());
        Store store = storeRepository.findByIdWithImages(storeId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.STORE_NOT_FOUND));
        hasStoreAuth(user, store);
        if (storeStatusUpdateRequest.status() == Status.PENDING) {
            throw new OminBusinessException(ErrorCode.STORE_STATUS_INVALID_CHANGE);
        }
        //가게 상태가 PENDING 일 때 예외발생
        if (store.getStatus() == Status.PENDING) {
            throw new OminBusinessException(ErrorCode.STORE_STATUS_PENDING_CANNOT_MODIFY);
        }
        Status prevStatus = store.getStatus();
        store.updateStatus(storeStatusUpdateRequest.status());
        log.info("매장 상태 변경 완료 - storeId: {}, {} -> {}", storeId, prevStatus, storeStatusUpdateRequest.status());
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
            throw new OminBusinessException(ErrorCode.STORE_ACCESS_DENIED);
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

    private static final int MAX_IMAGE_COUNT = 10;

    private static void processImages(Store store, List<StoreUpdateRequest.StoreImageRequest> imageRequests, List<String> newUrlList) {
        // action 유효성 검증: ADD는 id 없어야 하고, KEEP/DELETE는 id 필수 + 중복 id 금지
        Set<UUID> seenIds = new HashSet<>();
        for (StoreUpdateRequest.StoreImageRequest req : imageRequests) {
            boolean isAdd = req.action() == StoreUpdateRequest.ImageAction.ADD;
            if (isAdd && req.id() != null) {
                throw new OminBusinessException(ErrorCode.STORE_IMAGE_INVALID_ACTION);
            }
            if (!isAdd && req.id() == null) {
                throw new OminBusinessException(ErrorCode.STORE_IMAGE_INVALID_ACTION);
            }
            if (!isAdd && !seenIds.add(req.id())) {
                throw new OminBusinessException(ErrorCode.STORE_IMAGE_DUPLICATE_ID);
            }
        }

        // 최종 이미지 수 검증: KEEP + ADD 만 실제 이미지로 남음
        long finalImageCount = imageRequests.stream()
                .filter(r -> r.action() == StoreUpdateRequest.ImageAction.KEEP
                          || r.action() == StoreUpdateRequest.ImageAction.ADD)
                .count();
        if (finalImageCount > MAX_IMAGE_COUNT) {
            throw new OminBusinessException(ErrorCode.STORE_IMAGE_MAX_EXCEEDED);
        }
        log.debug("최종 이미지 수 검증 - finalCount: {}", finalImageCount);

        // DELETE 처리: 명시적으로 DELETE 표시된 이미지 제거
        Set<UUID> deleteIds = imageRequests.stream()
                .filter(r -> r.action() == StoreUpdateRequest.ImageAction.DELETE)
                .map(StoreUpdateRequest.StoreImageRequest::id)
                .collect(Collectors.toSet());
        log.debug("이미지 삭제 처리 - deleteIds: {}", deleteIds);
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
                        log.debug("이미지 ID 불일치 - imageId: {}", req.id());
                        throw new OminBusinessException(ErrorCode.STORE_IMAGE_NOT_FOUND);
                    }
                    img.updateImageSorting(sequence++);
                    log.debug("기존 이미지 순서 갱신 - imageId: {}, sequence: {}", req.id(), sequence - 1);
                }
                case ADD -> {
                    store.addNewImage(newUrlList.get(addIdx++), sequence++);
                    log.debug("신규 이미지 추가 - sequence: {}", sequence - 1);
                }
                case DELETE -> { /* 이미 처리됨 */ }
            }
        }
    }

    private static Store toEntity(StoreCreateRequest request, UUID ownerId, Point coordinates) {
        return Store.builder()
                .ownerId(ownerId)
                .category(request.category())
                .name(request.name())
                .roadAddress(request.roadAddress())
                .detailAddress(request.detailAddress())
                .coordinates(coordinates)
                .build();
    }

    private static Point toPoint(KakaoAddressClient.KakaoAddressResult kakao) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(
                kakao.longitude().doubleValue(),
                kakao.latitude().doubleValue()
        ));
    }

    private static Point toPoint(CoordinatesSearchDto dto) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(
                dto.longitude().doubleValue(),
                dto.latitude().doubleValue()
        ));
    }
}
