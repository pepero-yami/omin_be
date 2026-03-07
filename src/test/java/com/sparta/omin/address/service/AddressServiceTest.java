package com.sparta.omin.address.service;

import com.sparta.omin.app.model.address.dto.AddressCreateRequest;
import com.sparta.omin.app.model.address.dto.AddressResponse;
import com.sparta.omin.app.model.address.dto.AddressUpdateRequest;
import com.sparta.omin.app.model.address.entity.Address;
import com.sparta.omin.app.model.address.repos.AddressRepository;
import com.sparta.omin.app.model.address.service.AddressService;
import com.sparta.omin.app.model.region.client.KakaoAddressClient;
import com.sparta.omin.app.model.region.entity.Region;
import com.sparta.omin.app.model.region.repos.RegionRepository;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Address:Service")
class AddressServiceTest {

    @Mock private AddressRepository addressRepository;
    @Mock private RegionRepository regionRepository;
    @Mock private KakaoAddressClient kakaoAddressClient;
    @InjectMocks private AddressService addressService;

    private UUID userId;
    private Region region;
    private KakaoAddressClient.KakaoAddressResult kakaoResult;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        region = Region.create("서울 강남구 역삼동");
        kakaoResult = new KakaoAddressClient.KakaoAddressResult("서울 강남구 역삼동", new BigDecimal("37.5"), new BigDecimal("127.1"));
    }

    @Nested
    @DisplayName("주소 생성 테스트")
    class CreateAddress {
        @Test
        @DisplayName("첫 주소 등록 시 자동으로 기본 배송지로 설정된다")
        void create_firstAddress_setsDefaultTrue() {
            // Given
            // 신규 생성 중 기본 배송지 설정을 false로 보냈다고 가정
            AddressCreateRequest request = new AddressCreateRequest("집", "테헤란로", "101호", false);

            mockKakaoAndRegion();
            // 현재 저장된 주소가 0개라고 가정
            given(addressRepository.countByUserIdAndIsDeletedFalse(userId)).willReturn(0L);
            // Repository의 save 메서드가 호출되면, 받은 객체를 그대로 반환하라고 설정
            given(addressRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // When
            AddressResponse response = addressService.create(userId, request);

            // Then
            // 사용자는 false로 보냈지만, 첫 주소이므로 결과는 true여야 함!
            assertThat(response.isDefault()).isTrue();
        }

        @Test
        @DisplayName("이미 동일한 상세 주소가 존재하면 예외가 발생한다")
        void create_duplicateAddress_throwsException() {
            // Given
            AddressCreateRequest request = new AddressCreateRequest("집", "테헤란로", "101호", false);

            mockKakaoAndRegion();
            given(addressRepository.existsByUserIdAndRegionIdAndRoadAddressAndShippingDetailAddressAndIsDeletedFalse(any(), any(), any(), any()))
                    .willReturn(true);

            // When & Then
            assertThatThrownBy(() -> addressService.create(userId, request))
                    .isInstanceOf(OminBusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADDRESS_DUPLICATED);
        }
    }

    @Nested
    @DisplayName("주소 수정 및 삭제 테스트")
    class UpdateAndDeleteAddress {
        @Test
        @DisplayName("기본 배송지인 주소를 일반 주소로 변경하려고 하면 예외가 발생한다")
        void update_defaultToFalse_throwsException() {
            // Given
            UUID addressId = UUID.randomUUID();
            Address defaultAddress = createMockAddress(true);
            AddressUpdateRequest request = new AddressUpdateRequest("닉네임", "도로명", "상세", false);

            given(addressRepository.findByIdAndUserIdAndIsDeletedFalse(addressId, userId)).willReturn(Optional.of(defaultAddress));
            mockKakaoAndRegion();

            // When & Then
            assertThatThrownBy(() -> addressService.update(userId, addressId, request))
                    .isInstanceOf(OminBusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADDRESS_DEFAULT_MUST_EXIST);
        }

        @Test
        @DisplayName("기본 배송지는 삭제할 수 없다")
        void delete_defaultAddress_throwsException() {
            // Given
            UUID addressId = UUID.randomUUID();
            // 기본 배송지 상태인(true) 가짜 객체 생성
            Address defaultAddress = createMockAddress(true);

            // ID로 조회했을 때 이 가짜 객체가 나오도록 설정
            given(addressRepository.findByIdAndUserIdAndIsDeletedFalse(addressId, userId)).willReturn(Optional.of(defaultAddress));

            // When & Then
            assertThatThrownBy(() -> addressService.delete(userId, addressId))
                    .isInstanceOf(OminBusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADDRESS_DEFAULT_CANNOT_DELETE);
        }
    }

    @Nested
    @DisplayName("기본 배송지 설정 테스트")
    class SetDefaultAddress {
        @Test
        @DisplayName("특정 주소를 기본으로 설정하면 기존 기본 주소는 해제된다")
        void setDefault_switchesDefaultStatus() {
            // Given
            UUID newDefaultId = UUID.randomUUID();
            Address newDefaultAddress = createMockAddress(false);
            Address oldDefaultAddress = createMockAddress(true);

            given(addressRepository.findByIdAndUserIdAndIsDeletedFalse(newDefaultId, userId)).willReturn(Optional.of(newDefaultAddress));
            given(addressRepository.findByUserIdAndIsDefaultTrueAndIsDeletedFalse(userId)).willReturn(Optional.of(oldDefaultAddress));

            // When
            addressService.setDefault(userId, newDefaultId);

            // Then
            assertThat(newDefaultAddress.isDefault()).isTrue();
            assertThat(oldDefaultAddress.isDefault()).isFalse();
        }
    }

    private void mockKakaoAndRegion() {
        given(kakaoAddressClient.searchAddress(any())).willReturn(kakaoResult);
        given(regionRepository.findByAddressAndIsDeletedFalse(any())).willReturn(Optional.of(region));
    }

    private Address createMockAddress(boolean isDefault) {
        return Address.create(userId, UUID.randomUUID(), "닉네임", "도로명", "상세",
                BigDecimal.ZERO, BigDecimal.ZERO, isDefault);
    }
}