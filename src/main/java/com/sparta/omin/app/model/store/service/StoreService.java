package com.sparta.omin.app.model.store.service;

import com.sparta.omin.app.model.address.dto.CoordinatesSearchDto;
import com.sparta.omin.app.model.address.service.CoordinatesSearchService;
import com.sparta.omin.app.model.order.entity.status.OrderStatus;
import com.sparta.omin.app.model.order.service.OrderStatusCheckService;
import com.sparta.omin.app.model.region.client.KakaoAddressClient;
import com.sparta.omin.app.model.stats.entity.StoreRatingStat;
import com.sparta.omin.app.model.stats.service.StoreRatingStatService;
import com.sparta.omin.app.model.store.code.Status;
import com.sparta.omin.app.model.store.dto.request.*;
import com.sparta.omin.app.model.store.dto.response.StoreOwnerAdminSearchResponse;
import com.sparta.omin.app.model.store.dto.response.StoreResponse;
import com.sparta.omin.app.model.store.dto.response.StoreSearchResponse;
import com.sparta.omin.app.model.store.dto.response.StoreSliceResponse;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.store.repos.StoreRepository;
import com.sparta.omin.app.model.user.constants.Role;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.app.model.user.service.UserPromoteService;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import com.sparta.omin.common.util.ImageUploader;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreWriter storeWriter;
    private final KakaoAddressClient kakaoAddressClient;
    private final CoordinatesSearchService coordinatesSearchService;
    private final UserPromoteService userPromoteService;
    private final OrderStatusCheckService orderStatusCheckService;
    private final StoreRatingStatService storeRatingStatService;
    private final ImageUploader imageUploader;

    //point로 변환
    //4326 : 좌표 표준 코드
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    //이미지 업로드 허용 타입
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    //수락, 조리중일 땐 가게 상태를 영업종료로 변경할 수 없음
    private static final List<OrderStatus> ACTIVE_ORDER_STATUSES =
            List.of(OrderStatus.ACCEPTED, OrderStatus.COOKING);

    // 가게 생성
    // 외부 API/S3 호출을 트랜잭션 밖에서 실행할 수 있게 DB 저장은 StoreWriter에 위임
    public StoreResponse registerStore(StoreCreateRequest storeCreateRequest, List<MultipartFile> images, UserDetails user) {
        User loginUser = (User) user;
        log.debug("매장 등록 요청 - ownerId: {}, name: {}", loginUser.getId(), storeCreateRequest.name());

        //주소 중복 검증
        if (storeRepository.existsByRoadAddressAndDetailAddress(storeCreateRequest.roadAddress(), storeCreateRequest.detailAddress())) {
            throw new OminBusinessException(ErrorCode.STORE_DUPLICATE_ADDRESS);
        }
        //업로드 파일 타입 검증
        validateImageContentType(images);
        //카카오 api 호출
        KakaoAddressClient.KakaoAddressResult kakao = kakaoAddressClient.searchAddress(storeCreateRequest.roadAddress());
        Point coordinates = toPoint(kakao);
        //S3에 이미지 업로드
        List<String> imageUrlList = images.stream().map(imageUploader::uploadStoreImage).toList();

        return storeWriter.save(storeCreateRequest, loginUser.getId(), coordinates, imageUrlList);
    }

    // 단건조회
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
        //평균평점, 총 리뷰 수
        StoreRatingStat stat = storeRatingStatService.getStat(storeId).orElse(null);
        double avgRating = stat != null ? stat.getAvgRating() : 0.0;
        long totalReview = stat != null ? stat.getTotalReview() : 0L;
        return StoreResponse.of(store, avgRating, totalReview);
    }

    // 가게 수정
    // 외부 API/S3 호출을 트랜잭션 밖에서 실행, DB 수정은 StoreWriter에 위임
    public StoreResponse modifyStore(UUID storeId, StoreUpdateRequest storeUpdateRequest, List<MultipartFile> newImages, UserDetails user) {
        log.info("매장 수정 요청 - storeId: {}", storeId);
        Store savedStore = storeRepository.findByIdWithImages(storeId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.STORE_NOT_FOUND));
        hasStoreAuth(user, savedStore);

        if (storeRepository.existsByRoadAddressAndDetailAddressAndIdNot(storeUpdateRequest.roadAddress(), storeUpdateRequest.detailAddress(), storeId)) {
            throw new OminBusinessException(ErrorCode.STORE_DUPLICATE_ADDRESS);
        }
        //위경도 추출이 목적. 도로명주소만 비교.
        boolean addressChanged = !savedStore.getRoadAddress().equals(storeUpdateRequest.roadAddress());
        Point coordinates = addressChanged
                ? toPoint(kakaoAddressClient.searchAddress(storeUpdateRequest.roadAddress()))
                : savedStore.getCoordinates();
        log.debug("주소 변경 여부 - changed: {}", addressChanged);

        List<MultipartFile> imageFiles = newImages != null ? newImages : List.of();
        //추가한 이미지 수와 업로드된 파일 수가 일치하는지 검증
        long addCount = storeUpdateRequest.images().stream()
                .filter(r -> r.action() == StoreUpdateRequest.ImageAction.ADD)
                .count();
        if (addCount != imageFiles.size()) {
            throw new OminBusinessException(ErrorCode.STORE_IMAGE_COUNT_MISMATCH);
        }
        //새로 업로드 한 이미지 타입 검증
        if (!imageFiles.isEmpty()) {
            validateImageContentType(imageFiles);
        }
        //S3에 이미지 업로드
        List<String> newUrlList = imageFiles.stream().map(imageUploader::uploadStoreImage).toList();

        return storeWriter.update(storeId, storeUpdateRequest, coordinates, newUrlList);
    }

    // 가게 삭제
    @Transactional
    public void deleteStore(UUID storeId, UserDetails user) {
        log.info("매장 삭제 요청 - storeId: {}", storeId);
        Store store = storeRepository.findByIdWithImages(storeId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.STORE_NOT_FOUND));
        hasStoreAuth(user, store);
        if (store.getStatus() == Status.OPENED) {
            throw new OminBusinessException(ErrorCode.STORE_OPENED_CANNOT_DELETE);
        }
        UUID ownerId = store.getOwnerId();
        storeRepository.delete(store);
        log.info("매장 삭제 완료 - storeId: {}", storeId);

        // 삭제 후 점주의 잔여 가게가 없으면 OWNER -> CUSTOMER 강등
        if (storeRepository.countByOwnerId(ownerId) == 0) {
            userPromoteService.demoteToCustomerIfOwner(ownerId);
        }
    }

    // customer용 매장 조회 리스트
    public StoreSliceResponse<StoreSearchResponse> searchStoreList(StoreSearchRequest storeSearchRequest, UserDetails user) {
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
                        projection.getRoadAddress(), projection.getDetailAddress(), projection.getStatus(), projection.getDistance(),
                        projection.getAvgRating() != null ? projection.getAvgRating() : 0.0,
                        projection.getTotalReview() != null ? projection.getTotalReview() : 0L,
                        projection.getMainImage()
                ))
                .toList();

        //페이징처리
        Double nextLastDistance = null;
        UUID nextLastId = null;
        if (result.hasNext() && !result.getContent().isEmpty()) {
            //리스트의 제일 마지막 줄
            StoreRepository.StoreSearchProjection lastProjection = result.getContent().get(result.getContent().size() - 1);
            //다음페이지
            nextLastDistance = lastProjection.getDistance();
            nextLastId = lastProjection.getStoreId();
        }
        return StoreSliceResponse.ofDistanceCursor(content, result.hasNext(), nextLastDistance, nextLastId);
    }

    // 점주용: 본인 등록 매장 목록 조회
    public StoreSliceResponse<StoreOwnerAdminSearchResponse> findMyStores(StoreOwnerAdminSearchRequest cursorRequest, UserDetails user) {
        UUID ownerId = ((User) user).getId();
        log.debug("본인 매장 목록 조회 - ownerId: {}, lastCreatedAt: {}, size: {}", ownerId, cursorRequest.lastCreatedAt(), cursorRequest.size());
        Slice<Store> result = storeRepository.findByOwnerIdCursor(
                ownerId, cursorRequest.lastCreatedAt(), cursorRequest.lastId(),
                PageRequest.of(0, cursorRequest.size()));
        return toCreatedAtCursorResponse(result);
    }

    // 관리자용: PENDING 상태 매장 목록 조회
    public StoreSliceResponse<StoreOwnerAdminSearchResponse> findPendingStores(StoreOwnerAdminSearchRequest cursorRequest) {
        log.debug("PENDING 매장 목록 조회 - lastCreatedAt: {}, size: {}", cursorRequest.lastCreatedAt(), cursorRequest.size());
        Slice<Store> result = storeRepository.findByStatusCursor(
                Status.PENDING, cursorRequest.lastCreatedAt(), cursorRequest.lastId(),
                PageRequest.of(0, cursorRequest.size()));
        return toCreatedAtCursorResponse(result);
    }

    //생성일 기반 커서 페이징처리
    private static StoreSliceResponse<StoreOwnerAdminSearchResponse> toCreatedAtCursorResponse(Slice<Store> slice) {
        List<StoreOwnerAdminSearchResponse> content = slice.getContent().stream()
                .map(StoreOwnerAdminSearchResponse::of)
                .toList();
        LocalDateTime nextLastCreatedAt = null;
        UUID nextLastId = null;
        if (slice.hasNext() && !slice.getContent().isEmpty()) {
            Store last = slice.getContent().get(slice.getContent().size() - 1);
            nextLastCreatedAt = last.getCreatedAt();
            nextLastId = last.getId();
        }
        return StoreSliceResponse.ofCreatedAtCursor(content, slice.hasNext(), nextLastCreatedAt, nextLastId);
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
        User loginUser = (User) user;
        log.info("매장 상태 변경 요청 - storeId: {}, targetStatus: {}", storeId, storeStatusUpdateRequest.status());
        Store store = storeRepository.findByIdWithImages(storeId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.STORE_NOT_FOUND));
        hasStoreAuth(user, store);

        //점주는 가게상태를 승인대기로는 변경못함.
        if (storeStatusUpdateRequest.status() == Status.PENDING && !(loginUser.getRole() == Role.MANAGER || loginUser.getRole() == Role.MASTER)) {
            throw new OminBusinessException(ErrorCode.STORE_STATUS_INVALID_CHANGE);
        }
        //가게 상태가 PENDING 일 때 예외발생
        if (store.getStatus() == Status.PENDING) {
            throw new OminBusinessException(ErrorCode.STORE_STATUS_PENDING_CANNOT_MODIFY);
        }
        if (storeStatusUpdateRequest.status() == Status.CLOSED) {
            // 주문 수락, 조리중 일 때  CLOSED로 상태 변경 불가
            if (orderStatusCheckService.existsProcessingOrder(storeId, ACTIVE_ORDER_STATUSES)) {
                throw new OminBusinessException(ErrorCode.STORE_HAS_ACTIVE_ORDERS);
            }
        }
        Status prevStatus = store.getStatus();
        store.updateStatus(storeStatusUpdateRequest.status());
        log.info("매장 상태 변경 완료 - storeId: {}, {} -> {}", storeId, prevStatus, storeStatusUpdateRequest.status());
        return StoreResponse.of(store);
    }

    // 권한 검증
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

    // 업로드 타입 검증
    private static void validateImageContentType(List<MultipartFile> images) {
        for (MultipartFile image : images) {
            //빈 파일 업로드 x
            if (image.isEmpty()) {
                throw new OminBusinessException(ErrorCode.STORE_IMAGE_EMPTY);
            }
            String contentType = image.getContentType();
            //잘못된 파일 형식이 넘어왔을 경우
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
                throw new OminBusinessException(ErrorCode.STORE_IMAGE_INVALID_TYPE);
            }
        }
    }

    //위경도를 좌표객체타입으로 변환
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
