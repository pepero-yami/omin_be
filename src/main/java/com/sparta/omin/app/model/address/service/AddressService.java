package com.sparta.omin.app.model.address.service;

import com.sparta.omin.app.model.address.dto.AddressCreateRequest;
import com.sparta.omin.app.model.address.dto.AddressResponse;
import com.sparta.omin.app.model.address.dto.AddressUpdateRequest;
import com.sparta.omin.app.model.address.entity.Address;
import com.sparta.omin.app.model.address.repos.AddressRepository;
import com.sparta.omin.app.model.region.client.KakaoAddressClient;
import com.sparta.omin.app.model.region.client.KakaoAddressClient.KakaoAddressResult;
import com.sparta.omin.app.model.region.service.RegionService;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {

    private final AddressRepository addressRepository;
    private final RegionService regionService;
    private final KakaoAddressClient kakaoAddressClient;

    @Transactional
    public AddressResponse createAddress(UUID userId, AddressCreateRequest request) {
        // 카카오 API로 주소 정규화 + 좌표 획득 <- 정제된 주소가 나옴!
        KakaoAddressResult kakao = kakaoAddressClient.searchAddress(request.roadAddress().trim());

        // Region 조회 (법정동 기반 - RegionService 활용)
        UUID regionId = regionService.getRegionIdByAddress(kakao.depth3Address());

        // 중복 검사 (사용자 입력값이 아닌, 카카오가 준 정제된 주소 사용! 이러면 중간에 띄어쓰기 2번해도 ㄱㅊ)
        String cleanRoadAddress = kakao.roadAddress(); // 띄어쓰기가 정제된 주소
        String cleanDetail = request.shippingDetailAddress().trim(); //상세주소까지 동일해야 중복처리

        if (addressRepository.existsByUserIdAndRegionIdAndRoadAddressAndShippingDetailAddressAndIsDeletedFalse(
                userId, regionId, cleanRoadAddress, cleanDetail
        )) {
            throw new OminBusinessException(ErrorCode.ADDRESS_DUPLICATED);
        }

        // 기본 배송지 설정 로직 (주소 0개면 무조건 기본배송지, 아니면 요청에 따름)
        long count = addressRepository.countByUserIdAndIsDeletedFalse(userId);
        boolean isDefault = (count == 0) || Boolean.TRUE.equals(request.isDefault());

        // 만약 기본으로 설정될 경우, 기존의 기본은 해제처리
        if (isDefault) {
            addressRepository.findByUserIdAndIsDefaultTrueAndIsDeletedFalse(userId)
                    .ifPresent(a -> a.setDefault(false));
        }

        // 저장 (정제된 주소와 법정동 ID 저장!)
        Address saved = addressRepository.save(Address.create(
                userId,
                regionId,
                request.nickname().trim(),
                cleanRoadAddress,
                cleanDetail,
                kakao.latitude(),
                kakao.longitude(),
                isDefault
        ));

        return toResponse(saved);
    }



    @Transactional
    public AddressResponse updateAddress(UUID userId, UUID addressId, AddressUpdateRequest request) {
        Address address = addressRepository.findByIdAndUserIdAndIsDeletedFalse(addressId, userId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        // 카카오 API 정규화
        KakaoAddressResult kakao = kakaoAddressClient.searchAddress(request.roadAddress().trim());

        // Region 조회
        UUID regionId = regionService.getRegionIdByAddress(kakao.depth3Address());

        // 수정 시 -> 자기 자신 제외하고 상세주소까지 동일이면 중복
        String cleanRoadAddress = kakao.roadAddress();
        String cleanDetail = request.shippingDetailAddress().trim();

        if (addressRepository.existsByUserIdAndRegionIdAndRoadAddressAndShippingDetailAddressAndIsDeletedFalseAndIdNot(
                userId, regionId, cleanRoadAddress, cleanDetail, addressId
        )) {
            throw new OminBusinessException(ErrorCode.ADDRESS_DUPLICATED);
        }

        // 기본 배송지 로직 체크
        boolean wantDefault = Boolean.TRUE.equals(request.isDefault());

        // 기본배송지는 항상 1개 이상
        if (!wantDefault && address.isDefault()) {
            // 기본배송지를 false로 바꾸면 기본이 0개 될 수 있으니 막았음
            throw new OminBusinessException(ErrorCode.ADDRESS_DEFAULT_MUST_EXIST);
        }

        // 기본으로 바꾸려는 경우: 기존 기본 해제처리
        if (wantDefault) {
            addressRepository.findByUserIdAndIsDefaultTrueAndIsDeletedFalse(userId)
                    .filter(a -> !a.getId().equals(addressId))
                    .ifPresent(a -> a.setDefault(false));
        }

        address.update(
                regionId,
                request.nickname().trim(),
                cleanRoadAddress,
                cleanDetail,
                kakao.latitude(),
                kakao.longitude(),
                wantDefault
        );

        return toResponse(address);
    }

    // 페이징 처리 및 반환
    public Page<AddressResponse> getMyAddresses(UUID userId, Pageable pageable) {
        Pageable validatedPageable = validatePageSize(pageable);
        return addressRepository.findAllByUserIdAndIsDeletedFalse(userId, validatedPageable)
                .map(this::toResponse);
    }

    public AddressResponse getMyAddress(UUID userId, UUID addressId) {
        Address address = addressRepository.findByIdAndUserIdAndIsDeletedFalse(addressId, userId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.ADDRESS_NOT_FOUND));
        return toResponse(address);
    }

    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        Address address = addressRepository.findByIdAndUserIdAndIsDeletedFalse(addressId, userId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        if (address.isDefault()) {
            throw new OminBusinessException(ErrorCode.ADDRESS_DEFAULT_CANNOT_DELETE);
        }

        address.softDelete();
    }

    @Transactional
    public AddressResponse setDefaultAddress(UUID userId, UUID addressId) {
        Address address = addressRepository.findByIdAndUserIdAndIsDeletedFalse(addressId, userId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        if (address.isDefault()) {
            return toResponse(address);
        }

        addressRepository.findByUserIdAndIsDefaultTrueAndIsDeletedFalse(userId)
                .ifPresent(a -> a.setDefault(false));

        address.setDefault(true);
        return toResponse(address);
    }

    private Pageable validatePageSize(Pageable pageable) {
        int size = pageable.getPageSize();
        if (size != 10 && size != 30 && size != 50) {
            return PageRequest.of(pageable.getPageNumber(), 10, pageable.getSort());
        }
        return pageable;
    }

    private AddressResponse toResponse(Address address) {
        return AddressResponse.of(
                address.getId(),
                address.getRegionId(),
                address.getNickname(),
                address.getRoadAddress(),
                address.getShippingDetailAddress(),
                address.getAddressLat(),
                address.getAddressLong(),
                address.isDefault(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }
}