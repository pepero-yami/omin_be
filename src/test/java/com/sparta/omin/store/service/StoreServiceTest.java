package com.sparta.omin.store.service;

import com.sparta.omin.app.model.order.repos.OrderRepository;
import com.sparta.omin.app.model.region.client.KakaoAddressClient;
import com.sparta.omin.app.model.stats.entity.StoreRatingStat;
import com.sparta.omin.app.model.stats.service.StoreRatingStatService;
import com.sparta.omin.app.model.store.code.Category;
import com.sparta.omin.app.model.store.code.Status;
import com.sparta.omin.app.model.store.dto.request.StoreCreateRequest;
import com.sparta.omin.app.model.store.dto.request.StoreOwnerAdminSearchRequest;
import com.sparta.omin.app.model.store.dto.request.StoreStatusUpdateRequest;
import com.sparta.omin.app.model.store.dto.request.StoreUpdateRequest;
import com.sparta.omin.app.model.store.dto.response.StoreOwnerAdminSearchResponse;
import com.sparta.omin.app.model.store.dto.response.StoreResponse;
import com.sparta.omin.app.model.store.dto.response.StoreSliceResponse;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.store.repos.StoreRepository;
import com.sparta.omin.app.model.store.service.StoreService;
import com.sparta.omin.app.model.store.service.StoreWriter;
import com.sparta.omin.app.model.user.constants.Role;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.app.model.user.service.UserPromoteService;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import com.sparta.omin.common.util.ImageUploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Store:Service")
class StoreServiceTest {

    @InjectMocks
    private StoreService storeService;

    @Mock
    private StoreRepository storeRepository;
    @Mock
    private UserPromoteService userPromoteService;
    @Mock
    private KakaoAddressClient kakaoAddressClient;
    @Mock
    private StoreRatingStatService storeRatingStatService;
    @Mock
    private StoreWriter storeWriter;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ImageUploader imageUploader;

    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);
    private static final Point DUMMY_POINT = GF.createPoint(new Coordinate(127.0, 37.5));

    // ───── 헬퍼: 유저 생성 ─────

    private User customerUser() {
        return User.builder()
                .name("유저").nickname("배고파").email("user@test.com").password("pass1!")
                .build();
    }

    private User ownerUser(UUID ownerId) {
        User user = User.builder()
                .name("사장님").nickname("사장").email("owner@test.com").password("pass1!")
                .build();
        ReflectionTestUtils.setField(user, "id", ownerId);
        ReflectionTestUtils.setField(user, "role", Role.OWNER);
        return user;
    }

    private User managerUser() {
        User user = User.builder()
                .name("관리자").nickname("매니저").email("manager@test.com").password("pass1!")
                .build();
        ReflectionTestUtils.setField(user, "role", Role.MANAGER);
        return user;
    }

    private Store buildStore(UUID storeId, UUID ownerId, Status status) {
        Store store = Store.builder()
                .ownerId(ownerId)
                .category(Category.KOREAN)
                .name("테스트가게")
                .roadAddress("서울특별시 강남구 테헤란로 427")
                .detailAddress("1층")
                .coordinates(DUMMY_POINT)
                .build();
        ReflectionTestUtils.setField(store, "id", storeId);
        if (status != Status.PENDING) {
            store.updateStatus(status);
        }
        return store;
    }

    private Store buildStoreWithCreatedAt(UUID storeId, UUID ownerId, Status status, LocalDateTime createdAt) {
        Store store = buildStore(storeId, ownerId, status);
        ReflectionTestUtils.setField(store, "createdAt", createdAt);
        return store;
    }

    private KakaoAddressClient.KakaoAddressResult kakaoResult() {
        return new KakaoAddressClient.KakaoAddressResult(
                "강남구 역삼동",
                "서울특별시 강남구 테헤란로 427",
                new BigDecimal("37.5"),
                new BigDecimal("127.0")
        );
    }

    @Nested
    @DisplayName("registerStore - 가게 등록")
    class RegisterStore {

        private final MockMultipartFile jpgImage = new MockMultipartFile(
                "images", "store.jpg", "image/jpeg", "img".getBytes());

        @BeforeEach
        void setUp() {
            lenient().when(imageUploader.uploadStoreImage(any())).thenReturn("https://s3.example.com/store.jpg");
        }

        @Test
        @DisplayName("성공: 정상 요청 시 StoreResponse 반환")
        void success() {
            UUID ownerId = UUID.randomUUID();
            User owner = ownerUser(ownerId);
            StoreCreateRequest req = new StoreCreateRequest(
                    Category.KOREAN, "테스트가게", "서울특별시 강남구 테헤란로 427", "1층");

            given(storeRepository.existsByRoadAddressAndDetailAddress(any(), any())).willReturn(false);
            given(kakaoAddressClient.searchAddress(any())).willReturn(kakaoResult());
            StoreResponse expected = StoreResponse.of(buildStore(UUID.randomUUID(), ownerId, Status.PENDING));
            given(storeWriter.save(any(), any(), any(), any())).willReturn(expected);

            StoreResponse result = storeService.registerStore(req, List.of(jpgImage), owner);

            assertThat(result).isNotNull();
            verify(storeWriter).save(eq(req), eq(ownerId), any(Point.class), any());
        }

        @Test
        @DisplayName("실패: 동일 주소 가게가 이미 존재하면 STORE_DUPLICATE_ADDRESS")
        void duplicateAddress() {
            User owner = ownerUser(UUID.randomUUID());
            StoreCreateRequest req = new StoreCreateRequest(
                    Category.KOREAN, "테스트가게", "서울특별시 강남구 테헤란로 427", "1층");

            given(storeRepository.existsByRoadAddressAndDetailAddress(any(), any())).willReturn(true);

            assertThatThrownBy(() -> storeService.registerStore(req, List.of(jpgImage), owner))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_DUPLICATE_ADDRESS);
        }

        @Test
        @DisplayName("실패: 빈 파일 업로드 시 STORE_IMAGE_EMPTY")
        void emptyFile() {
            User owner = ownerUser(UUID.randomUUID());
            StoreCreateRequest req = new StoreCreateRequest(
                    Category.KOREAN, "테스트가게", "서울특별시 강남구 테헤란로 427", "1층");
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "images", "empty.jpg", "image/jpeg", new byte[0]);

            given(storeRepository.existsByRoadAddressAndDetailAddress(any(), any())).willReturn(false);

            assertThatThrownBy(() -> storeService.registerStore(req, List.of(emptyFile), owner))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_IMAGE_EMPTY);
        }

        @Test
        @DisplayName("실패: 허용되지 않는 이미지 타입이면 STORE_IMAGE_INVALID_TYPE")
        void invalidImageType() {
            User owner = ownerUser(UUID.randomUUID());
            StoreCreateRequest req = new StoreCreateRequest(
                    Category.KOREAN, "테스트가게", "서울특별시 강남구 테헤란로 427", "1층");
            MockMultipartFile gifImage = new MockMultipartFile(
                    "images", "store.gif", "image/gif", "img".getBytes());

            given(storeRepository.existsByRoadAddressAndDetailAddress(any(), any())).willReturn(false);

            assertThatThrownBy(() -> storeService.registerStore(req, List.of(gifImage), owner))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_IMAGE_INVALID_TYPE);
        }
    }

    @Nested
    @DisplayName("findStore - 가게 단건 조회")
    class FindStore {

        @Test
        @DisplayName("성공: OPENED 가게는 일반 유저도 조회 가능")
        void openedStore_anyUser() {
            UUID storeId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Store store = buildStore(storeId, ownerId, Status.OPENED);
            User customer = customerUser();

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));
            given(storeRatingStatService.getStat(storeId)).willReturn(Optional.empty());

            StoreResponse result = storeService.findStore(storeId, customer);

            assertThat(result.id()).isEqualTo(storeId);
            assertThat(result.status()).isEqualTo(Status.OPENED);
        }

        @Test
        @DisplayName("성공: PENDING 가게는 관리자가 조회 가능")
        void pendingStore_managerCanView() {
            UUID storeId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Store store = buildStore(storeId, ownerId, Status.PENDING);
            User manager = managerUser();

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));
            given(storeRatingStatService.getStat(storeId)).willReturn(Optional.empty());

            StoreResponse result = storeService.findStore(storeId, manager);

            assertThat(result.status()).isEqualTo(Status.PENDING);
        }

        @Test
        @DisplayName("성공: PENDING 가게는 점주 본인이 조회 가능")
        void pendingStore_ownerCanView() {
            UUID storeId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Store store = buildStore(storeId, ownerId, Status.PENDING);
            User owner = ownerUser(ownerId);

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));
            given(storeRatingStatService.getStat(storeId)).willReturn(Optional.empty());

            StoreResponse result = storeService.findStore(storeId, owner);

            assertThat(result.status()).isEqualTo(Status.PENDING);
        }

        @Test
        @DisplayName("성공: 평점 stat이 있으면 응답에 반영")
        void withRatingStat() {
            UUID storeId = UUID.randomUUID();
            Store store = buildStore(storeId, UUID.randomUUID(), Status.OPENED);
            User customer = customerUser();

            StoreRatingStat stat = mock(StoreRatingStat.class);
            given(stat.getAvgRating()).willReturn(4.5);
            given(stat.getTotalReview()).willReturn(10L);

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));
            given(storeRatingStatService.getStat(storeId)).willReturn(Optional.of(stat));

            StoreResponse result = storeService.findStore(storeId, customer);

            assertThat(result.avgRating()).isEqualTo(4.5);
            assertThat(result.totalReview()).isEqualTo(10L);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 가게 ID → STORE_NOT_FOUND")
        void notFound() {
            UUID storeId = UUID.randomUUID();
            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> storeService.findStore(storeId, customerUser()))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: PENDING 가게를 타 유저가 조회하면 STORE_NOT_FOUND")
        void pendingStore_otherUserCannotView() {
            UUID storeId = UUID.randomUUID();
            Store store = buildStore(storeId, UUID.randomUUID(), Status.PENDING);
            User otherOwner = ownerUser(UUID.randomUUID());

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));

            assertThatThrownBy(() -> storeService.findStore(storeId, otherOwner))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("modifyStore - 가게 수정")
    class ModifyStore {

        private final StoreUpdateRequest keepRequest = new StoreUpdateRequest(
                Category.KOREAN, "수정가게",
                "서울특별시 강남구 테헤란로 427", "1층",
                List.of(new StoreUpdateRequest.StoreImageRequest(UUID.randomUUID(), StoreUpdateRequest.ImageAction.KEEP))
        );

        @Test
        @DisplayName("성공: 주소 변경 없이 정보 수정")
        void success_noAddressChange() {
            UUID storeId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Store store = buildStore(storeId, ownerId, Status.CLOSED);
            User owner = ownerUser(ownerId);

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));
            given(storeRepository.existsByRoadAddressAndDetailAddressAndIdNot(any(), any(), any())).willReturn(false);
            given(storeWriter.update(any(), any(), any(), any()))
                    .willReturn(StoreResponse.of(store));

            StoreResponse result = storeService.modifyStore(storeId, keepRequest, null, owner);

            assertThat(result).isNotNull();
            verify(kakaoAddressClient, never()).searchAddress(any());
        }

        @Test
        @DisplayName("성공: 주소 변경 시 카카오 API 호출")
        void success_addressChanged() {
            UUID storeId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Store store = buildStore(storeId, ownerId, Status.CLOSED);
            User owner = ownerUser(ownerId);
            StoreUpdateRequest newAddressRequest = new StoreUpdateRequest(
                    Category.KOREAN, "수정가게",
                    "서울특별시 서초구 강남대로 465", "2층",  // 주소 변경
                    List.of(new StoreUpdateRequest.StoreImageRequest(UUID.randomUUID(), StoreUpdateRequest.ImageAction.KEEP))
            );

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));
            given(storeRepository.existsByRoadAddressAndDetailAddressAndIdNot(any(), any(), any())).willReturn(false);
            given(kakaoAddressClient.searchAddress(any())).willReturn(kakaoResult());
            given(storeWriter.update(any(), any(), any(), any())).willReturn(StoreResponse.of(store));

            storeService.modifyStore(storeId, newAddressRequest, null, owner);

            verify(kakaoAddressClient).searchAddress(any());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 가게 → STORE_NOT_FOUND")
        void notFound() {
            UUID storeId = UUID.randomUUID();
            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> storeService.modifyStore(storeId, keepRequest, null, ownerUser(UUID.randomUUID())))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 권한 없는 유저가 수정 시도 → STORE_ACCESS_DENIED")
        void accessDenied() {
            UUID storeId = UUID.randomUUID();
            Store store = buildStore(storeId, UUID.randomUUID(), Status.CLOSED);
            User otherOwner = ownerUser(UUID.randomUUID());

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));

            assertThatThrownBy(() -> storeService.modifyStore(storeId, keepRequest, null, otherOwner))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_ACCESS_DENIED);
        }

        @Test
        @DisplayName("실패: ADD 이미지 수와 파일 수 불일치 → STORE_IMAGE_COUNT_MISMATCH")
        void imageCountMismatch() {
            UUID storeId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Store store = buildStore(storeId, ownerId, Status.CLOSED);
            User owner = ownerUser(ownerId);
            StoreUpdateRequest addRequest = new StoreUpdateRequest(
                    Category.KOREAN, "수정가게",
                    "서울특별시 강남구 테헤란로 427", "1층",
                    List.of(new StoreUpdateRequest.StoreImageRequest(null, StoreUpdateRequest.ImageAction.ADD))
            );

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));
            given(storeRepository.existsByRoadAddressAndDetailAddressAndIdNot(any(), any(), any())).willReturn(false);

            // ADD 1개인데 파일은 0개
            assertThatThrownBy(() -> storeService.modifyStore(storeId, addRequest, null, owner))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_IMAGE_COUNT_MISMATCH);
        }

        @Test
        @DisplayName("실패: 새 이미지에 빈 파일 포함 시 → STORE_IMAGE_EMPTY")
        void emptyImageFile() {
            UUID storeId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Store store = buildStore(storeId, ownerId, Status.CLOSED);
            User owner = ownerUser(ownerId);
            StoreUpdateRequest addRequest = new StoreUpdateRequest(
                    Category.KOREAN, "수정가게",
                    "서울특별시 강남구 테헤란로 427", "1층",
                    List.of(new StoreUpdateRequest.StoreImageRequest(null, StoreUpdateRequest.ImageAction.ADD))
            );
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "newImages", "empty.jpg", "image/jpeg", new byte[0]);

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));
            given(storeRepository.existsByRoadAddressAndDetailAddressAndIdNot(any(), any(), any())).willReturn(false);

            assertThatThrownBy(() -> storeService.modifyStore(storeId, addRequest, List.of(emptyFile), owner))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_IMAGE_EMPTY);
        }

        @Test
        @DisplayName("실패: 중복 주소 → STORE_DUPLICATE_ADDRESS")
        void duplicateAddress() {
            UUID storeId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Store store = buildStore(storeId, ownerId, Status.CLOSED);
            User owner = ownerUser(ownerId);

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));
            given(storeRepository.existsByRoadAddressAndDetailAddressAndIdNot(any(), any(), any())).willReturn(true);

            assertThatThrownBy(() -> storeService.modifyStore(storeId, keepRequest, null, owner))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_DUPLICATE_ADDRESS);
        }
    }

    @Nested
    @DisplayName("deleteStore - 가게 삭제")
    class DeleteStore {

        @Test
        @DisplayName("성공: CLOSED 가게 삭제, 잔여 가게 없으면 OWNER→CUSTOMER 강등")
        void success_demotesOwner() {
            UUID storeId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Store store = buildStore(storeId, ownerId, Status.CLOSED);
            User owner = ownerUser(ownerId);

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));
            given(storeRepository.countByOwnerId(ownerId)).willReturn(0L);

            storeService.deleteStore(storeId, owner);

            verify(storeRepository).delete(store);
            verify(userPromoteService).demoteToCustomerIfOwner(ownerId);
        }

        @Test
        @DisplayName("성공: 삭제 후 다른 가게가 남아있으면 강등하지 않음")
        void success_noDowngrade_whenOtherStoresExist() {
            UUID storeId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Store store = buildStore(storeId, ownerId, Status.CLOSED);
            User owner = ownerUser(ownerId);

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));
            given(storeRepository.countByOwnerId(ownerId)).willReturn(1L);

            storeService.deleteStore(storeId, owner);

            verify(storeRepository).delete(store);
            verify(userPromoteService, never()).demoteToCustomerIfOwner(any());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 가게 → STORE_NOT_FOUND")
        void notFound() {
            UUID storeId = UUID.randomUUID();
            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> storeService.deleteStore(storeId, ownerUser(UUID.randomUUID())))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 다른 사람의 가게 삭제 시도 → STORE_ACCESS_DENIED")
        void accessDenied() {
            UUID storeId = UUID.randomUUID();
            Store store = buildStore(storeId, UUID.randomUUID(), Status.CLOSED);
            User otherOwner = ownerUser(UUID.randomUUID());

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));

            assertThatThrownBy(() -> storeService.deleteStore(storeId, otherOwner))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_ACCESS_DENIED);
        }

        @Test
        @DisplayName("실패: OPENED 가게는 삭제 불가 → STORE_OPENED_CANNOT_DELETE")
        void openedCannotDelete() {
            UUID storeId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Store store = buildStore(storeId, ownerId, Status.OPENED);
            User owner = ownerUser(ownerId);

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));

            assertThatThrownBy(() -> storeService.deleteStore(storeId, owner))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_OPENED_CANNOT_DELETE);
        }
    }

    @Nested
    @DisplayName("findMyStores - 점주 본인 매장 목록 조회 (커서 기반)")
    class FindMyStores {

        @Test
        @DisplayName("성공: 첫 페이지 조회 - lastCreatedAt/lastId 없이 전체 반환")
        void firstPage_noNextCursor() {
            UUID ownerId = UUID.randomUUID();
            User owner = ownerUser(ownerId);
            Store store1 = buildStore(UUID.randomUUID(), ownerId, Status.OPENED);
            Store store2 = buildStore(UUID.randomUUID(), ownerId, Status.CLOSED);
            StoreOwnerAdminSearchRequest req = new StoreOwnerAdminSearchRequest(null, null, 10);

            given(storeRepository.findByOwnerIdCursor(eq(ownerId), isNull(), isNull(), any(Pageable.class)))
                    .willReturn(new SliceImpl<>(List.of(store1, store2), Pageable.ofSize(10), false));

            StoreSliceResponse<StoreOwnerAdminSearchResponse> result = storeService.findMyStores(req, owner);

            assertThat(result.content()).hasSize(2);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.nextLastCreatedAt()).isNull();
            assertThat(result.nextLastId()).isNull();
        }

        @Test
        @DisplayName("성공: hasNext=true 이면 nextLastCreatedAt/nextLastId 반환")
        void hasNext_returnsCursorValues() {
            UUID ownerId = UUID.randomUUID();
            User owner = ownerUser(ownerId);
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 12, 0);
            UUID lastStoreId = UUID.randomUUID();
            Store lastStore = buildStoreWithCreatedAt(lastStoreId, ownerId, Status.OPENED, createdAt);
            StoreOwnerAdminSearchRequest req = new StoreOwnerAdminSearchRequest(null, null, 10);

            given(storeRepository.findByOwnerIdCursor(eq(ownerId), isNull(), isNull(), any(Pageable.class)))
                    .willReturn(new SliceImpl<>(List.of(lastStore), Pageable.ofSize(10), true));

            StoreSliceResponse<StoreOwnerAdminSearchResponse> result = storeService.findMyStores(req, owner);

            assertThat(result.hasNext()).isTrue();
            assertThat(result.nextLastCreatedAt()).isEqualTo(createdAt);
            assertThat(result.nextLastId()).isEqualTo(lastStoreId);
        }

        @Test
        @DisplayName("성공: 커서 전달 시 해당 지점부터 조회")
        void withCursor_queriesFromCursor() {
            UUID ownerId = UUID.randomUUID();
            User owner = ownerUser(ownerId);
            LocalDateTime lastCreatedAt = LocalDateTime.of(2025, 1, 1, 12, 0);
            UUID lastId = UUID.randomUUID();
            StoreOwnerAdminSearchRequest req = new StoreOwnerAdminSearchRequest(lastCreatedAt, lastId, 10);

            given(storeRepository.findByOwnerIdCursor(eq(ownerId), eq(lastCreatedAt), eq(lastId), any(Pageable.class)))
                    .willReturn(new SliceImpl<>(List.of(), Pageable.ofSize(10), false));

            StoreSliceResponse<StoreOwnerAdminSearchResponse> result = storeService.findMyStores(req, owner);

            assertThat(result.content()).isEmpty();
            assertThat(result.hasNext()).isFalse();
            verify(storeRepository).findByOwnerIdCursor(eq(ownerId), eq(lastCreatedAt), eq(lastId), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("findPendingStores - PENDING 매장 목록 조회 (커서 기반)")
    class FindPendingStores {

        @Test
        @DisplayName("성공: 첫 페이지 조회 - PENDING 매장만 반환")
        void firstPage_returnsPendingStores() {
            UUID ownerId = UUID.randomUUID();
            Store pendingStore = buildStore(UUID.randomUUID(), ownerId, Status.PENDING);
            StoreOwnerAdminSearchRequest req = new StoreOwnerAdminSearchRequest(null, null, 10);

            given(storeRepository.findByStatusCursor(eq(Status.PENDING), isNull(), isNull(), any(Pageable.class)))
                    .willReturn(new SliceImpl<>(List.of(pendingStore), Pageable.ofSize(10), false));

            StoreSliceResponse<StoreOwnerAdminSearchResponse> result = storeService.findPendingStores(req);

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).status()).isEqualTo(Status.PENDING);
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("성공: hasNext=true이면 커서 값 반환")
        void hasNext_returnsCursorValues() {
            LocalDateTime createdAt = LocalDateTime.of(2025, 3, 1, 9, 0);
            UUID lastStoreId = UUID.randomUUID();
            Store lastStore = buildStoreWithCreatedAt(lastStoreId, UUID.randomUUID(), Status.PENDING, createdAt);
            StoreOwnerAdminSearchRequest req = new StoreOwnerAdminSearchRequest(null, null, 10);

            given(storeRepository.findByStatusCursor(eq(Status.PENDING), isNull(), isNull(), any(Pageable.class)))
                    .willReturn(new SliceImpl<>(List.of(lastStore), Pageable.ofSize(10), true));

            StoreSliceResponse<StoreOwnerAdminSearchResponse> result = storeService.findPendingStores(req);

            assertThat(result.hasNext()).isTrue();
            assertThat(result.nextLastCreatedAt()).isEqualTo(createdAt);
            assertThat(result.nextLastId()).isEqualTo(lastStoreId);
        }

        @Test
        @DisplayName("성공: 커서 전달 시 해당 지점부터 조회")
        void withCursor_queriesFromCursor() {
            LocalDateTime lastCreatedAt = LocalDateTime.of(2025, 3, 1, 9, 0);
            UUID lastId = UUID.randomUUID();
            StoreOwnerAdminSearchRequest req = new StoreOwnerAdminSearchRequest(lastCreatedAt, lastId, 10);

            given(storeRepository.findByStatusCursor(eq(Status.PENDING), eq(lastCreatedAt), eq(lastId), any(Pageable.class)))
                    .willReturn(new SliceImpl<>(List.of(), Pageable.ofSize(10), false));

            StoreSliceResponse<StoreOwnerAdminSearchResponse> result = storeService.findPendingStores(req);

            assertThat(result.content()).isEmpty();
            verify(storeRepository).findByStatusCursor(eq(Status.PENDING), eq(lastCreatedAt), eq(lastId), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("approveAndCloseStore - 관리자 가게 승인")
    class ApproveAndCloseStore {

        @Test
        @DisplayName("성공: PENDING 가게를 CLOSED로 변경 + 점주 OWNER 룰 변경")
        void success() {
            UUID storeId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Store store = buildStore(storeId, ownerId, Status.PENDING);
            User manager = managerUser();

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));

            StoreResponse result = storeService.approveAndCloseStore(storeId, manager);

            assertThat(result.status()).isEqualTo(Status.CLOSED);
            verify(userPromoteService).promoteToOwnerIfCustomer(ownerId);
        }

        @Test
        @DisplayName("실패: 관리자가 아닌 유저가 승인 시도 → STORE_ACCESS_DENIED")
        void notAdmin() {
            UUID ownerId = UUID.randomUUID();
            User owner = ownerUser(ownerId);

            assertThatThrownBy(() -> storeService.approveAndCloseStore(UUID.randomUUID(), owner))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_ACCESS_DENIED);
        }

        @Test
        @DisplayName("실패: 가게가 없으면 → STORE_NOT_FOUND")
        void notFound() {
            UUID storeId = UUID.randomUUID();
            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> storeService.approveAndCloseStore(storeId, managerUser()))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: PENDING이 아닌 가게 승인 시도 → STORE_STATUS_NOT_PENDING")
        void notPending() {
            UUID storeId = UUID.randomUUID();
            Store store = buildStore(storeId, UUID.randomUUID(), Status.CLOSED);
            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));

            assertThatThrownBy(() -> storeService.approveAndCloseStore(storeId, managerUser()))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_STATUS_NOT_PENDING);
        }
    }

    @Nested
    @DisplayName("modifyStoreStatus - 영업 상태 변경")
    class ModifyStoreStatus {

        @Test
        @DisplayName("성공: CLOSED → OPENED")
        void closedToOpened() {
            UUID storeId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Store store = buildStore(storeId, ownerId, Status.CLOSED);
            User owner = ownerUser(ownerId);
            StoreStatusUpdateRequest req = new StoreStatusUpdateRequest(Status.OPENED);

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));

            StoreResponse result = storeService.modifyStoreStatus(req, storeId, owner);

            assertThat(result.status()).isEqualTo(Status.OPENED);
        }

        @Test
        @DisplayName("성공: OPENED → CLOSED (진행 중 주문 없음)")
        void openedToClosed_noActiveOrders() {
            UUID storeId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Store store = buildStore(storeId, ownerId, Status.OPENED);
            User owner = ownerUser(ownerId);
            StoreStatusUpdateRequest req = new StoreStatusUpdateRequest(Status.CLOSED);

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));
            given(orderRepository.existsByStoreIdAndStatusInAndIsDeletedFalse(eq(storeId), anyList())).willReturn(false);

            StoreResponse result = storeService.modifyStoreStatus(req, storeId, owner);

            assertThat(result.status()).isEqualTo(Status.CLOSED);
        }

        @Test
        @DisplayName("실패: 점주가 PENDING 상태로 변경 시도 → STORE_STATUS_INVALID_CHANGE")
        void toPending_invalid() {
            UUID storeId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Store store = buildStore(storeId, ownerId, Status.CLOSED);
            User owner = ownerUser(ownerId);
            StoreStatusUpdateRequest req = new StoreStatusUpdateRequest(Status.PENDING);

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));

            assertThatThrownBy(() -> storeService.modifyStoreStatus(req, storeId, owner))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_STATUS_INVALID_CHANGE);
        }

        @Test
        @DisplayName("실패: PENDING 가게의 상태 변경 → STORE_STATUS_PENDING_CANNOT_MODIFY")
        void pendingStore_cannotModify() {
            UUID storeId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Store store = buildStore(storeId, ownerId, Status.PENDING);
            User owner = ownerUser(ownerId);
            StoreStatusUpdateRequest req = new StoreStatusUpdateRequest(Status.OPENED);

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));

            assertThatThrownBy(() -> storeService.modifyStoreStatus(req, storeId, owner))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_STATUS_PENDING_CANNOT_MODIFY);
        }

        @Test
        @DisplayName("실패: 진행 중인 주문이 있으면 CLOSED 전환 불가 → STORE_HAS_ACTIVE_ORDERS")
        void hasActiveOrders() {
            UUID storeId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Store store = buildStore(storeId, ownerId, Status.OPENED);
            User owner = ownerUser(ownerId);
            StoreStatusUpdateRequest req = new StoreStatusUpdateRequest(Status.CLOSED);

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));
            given(orderRepository.existsByStoreIdAndStatusInAndIsDeletedFalse(eq(storeId), anyList())).willReturn(true);

            assertThatThrownBy(() -> storeService.modifyStoreStatus(req, storeId, owner))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_HAS_ACTIVE_ORDERS);
        }

        @Test
        @DisplayName("실패: 권한 없는 유저가 상태 변경 → STORE_ACCESS_DENIED")
        void accessDenied() {
            UUID storeId = UUID.randomUUID();
            Store store = buildStore(storeId, UUID.randomUUID(), Status.CLOSED);
            User otherOwner = ownerUser(UUID.randomUUID());
            StoreStatusUpdateRequest req = new StoreStatusUpdateRequest(Status.OPENED);

            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.of(store));

            assertThatThrownBy(() -> storeService.modifyStoreStatus(req, storeId, otherOwner))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_ACCESS_DENIED);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 가게 → STORE_NOT_FOUND")
        void notFound() {
            UUID storeId = UUID.randomUUID();
            given(storeRepository.findByIdWithImages(storeId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> storeService.modifyStoreStatus(
                    new StoreStatusUpdateRequest(Status.OPENED), storeId, ownerUser(UUID.randomUUID())))
                    .isInstanceOf(OminBusinessException.class)
                    .extracting(e -> ((OminBusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STORE_NOT_FOUND);
        }
    }
}
