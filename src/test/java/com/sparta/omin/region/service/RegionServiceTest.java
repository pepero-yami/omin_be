package com.sparta.omin.region.service;

import com.sparta.omin.app.model.region.client.KakaoAddressClient;
import com.sparta.omin.app.model.region.dto.RegionCreateRequest;
import com.sparta.omin.app.model.region.dto.RegionResponse;
import com.sparta.omin.app.model.region.entity.Region;
import com.sparta.omin.app.model.region.repos.RegionRepository;
import com.sparta.omin.app.model.region.service.RegionService;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Region:Service")
class RegionServiceTest {

    @Mock private RegionRepository regionRepository;
    @Mock private KakaoAddressClient kakaoAddressClient;
    @InjectMocks private RegionService regionService;

    @Nested
    @DisplayName("지역 생성 테스트")
    class CreateRegion {
        @Test
        @DisplayName("새로운 지역을 성공적으로 생성한다")
        void createRegion_success() {
            // Given
            String rawAddress = "서울특별시 강남구 역삼동";
            String normalizedAddress = "서울 강남구 역삼동";
            RegionCreateRequest request = new RegionCreateRequest(rawAddress);
            Region region = Region.create(normalizedAddress);

            // 2. Kakao Client Mocking 추가
            // 카카오 API를 부르면 정규화된 주소를 돌려주도록 설정
            given(kakaoAddressClient.normalizeToRegionDepth3(rawAddress)).willReturn(normalizedAddress);
            // DB에 이 주소가 없다고 설정
            given(regionRepository.existsByAddressAndIsDeletedFalse(normalizedAddress)).willReturn(false);
            given(regionRepository.save(any(Region.class))).willReturn(region);

            // When
            RegionResponse response = regionService.createRegion(request);

            // Then
            assertThat(response.address()).isEqualTo(normalizedAddress);
        }

        @Test
        @DisplayName("이미 존재하는 주소로 지역을 생성하려 하면 예외가 발생한다")
        void createRegion_duplicateAddress_throwsException() {
            // Given
            String rawAddress = "서울 강남구 역삼동";
            String normalizedAddress = "서울특별시 강남구 역삼동";
            RegionCreateRequest request = new RegionCreateRequest(rawAddress);

            // 2. Kakao Client Mocking 추가
            given(kakaoAddressClient.normalizeToRegionDepth3(rawAddress)).willReturn(normalizedAddress);
            given(regionRepository.existsByAddressAndIsDeletedFalse(normalizedAddress)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> regionService.createRegion(request))
                    .isInstanceOf(OminBusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REGION_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("지역 조회 및 검색 테스트")
    class GetAndSearchRegion {
        @Test
        @DisplayName("ID로 지역을 조회할 때 존재하지 않으면 예외가 발생한다")
        void getRegion_notFound_throwsException() {
            // Given
            UUID id = UUID.randomUUID();
            given(regionRepository.findByIdAndIsDeletedFalse(id)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> regionService.getRegion(id))
                    .isInstanceOf(OminBusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REGION_NOT_FOUND);
        }

        @Test
        @DisplayName("키워드로 지역을 검색하면 필터링된 목록을 반환한다")
        void searchRegions_returnsFilteredList() {
            // Given
            String keyword = "강남";
            List<Region> regions = List.of(
                    Region.create("서울특별시 강남구 역삼동"),
                    Region.create("서울특별시 강남구 삼성동")
            );
            given(regionRepository.findAllByAddressContainingAndIsDeletedFalseOrderByAddressAsc(keyword))
                    .willReturn(regions);

            // When
            List<RegionResponse> result = regionService.searchRegions(keyword);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).address()).contains(keyword);
        }
    }

    @Nested
    @DisplayName("지역 삭제 테스트")
    class DeleteRegion {
        @Test
        @DisplayName("지역을 성공적으로 소프트 삭제한다")
        void deleteRegion_success() {
            // Given
            UUID id = UUID.randomUUID();
            Region region = Region.create("삭제할 주소");
            given(regionRepository.findByIdAndIsDeletedFalse(id)).willReturn(Optional.of(region));

            // When
            regionService.deleteRegion(id);

            // Then
            assertThat(region.isDeleted()).isTrue();
        }
    }
}