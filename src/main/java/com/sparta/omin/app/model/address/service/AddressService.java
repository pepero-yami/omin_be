package com.sparta.omin.app.model.address.service;

import com.sparta.omin.app.model.address.dto.AddressCreateRequest;
import com.sparta.omin.app.model.address.dto.AddressResponse;
import com.sparta.omin.app.model.address.dto.AddressUpdateRequest;
import com.sparta.omin.app.model.address.entity.Address;
import com.sparta.omin.app.model.address.repos.AddressRepository;
import com.sparta.omin.app.model.region.client.KakaoAddressClient;
import com.sparta.omin.app.model.region.client.KakaoAddressClient.KakaoAddressResult;
import com.sparta.omin.app.model.region.entity.Region;
import com.sparta.omin.app.model.region.repos.RegionRepository;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {

    private final AddressRepository addressRepository;
    private final RegionRepository regionRepository; //FIXME
    private final KakaoAddressClient kakaoAddressClient;

    @Transactional
    public AddressResponse createAddress(UUID userId, AddressCreateRequest request) {
        String rawRoadAddress = request.roadAddress().trim();
        String rawDetail = request.shippingDetailAddress().trim();

        KakaoAddressResult kakao = kakaoAddressClient.searchAddress(rawRoadAddress);

        // 단일 주소 조회로 간결화
        Region region = regionRepository.findByAddressAndIsDeletedFalse(kakao.depth3Address())
                .orElseThrow(() -> new OminBusinessException(ErrorCode.ADDRESS_REGION_NOT_FOUND));

        long count = addressRepository.countByUserIdAndIsDeletedFalse(userId);

        // 주소가 0개면 무조건 기본배송지
        boolean isDefault = (count == 0) || Boolean.TRUE.equals(request.isDefault());

        // 상세주소까지 동일해야만 중복
        if (addressRepository.existsByUserIdAndRegionIdAndRoadAddressAndShippingDetailAddressAndIsDeletedFalse(
                userId, region.getId(), rawRoadAddress, rawDetail
        )) {
            throw new OminBusinessException(ErrorCode.ADDRESS_DUPLICATED);
        }

        // 기본으로 생성될 경우 기존 기본 해제
        if (isDefault) {
            addressRepository.findByUserIdAndIsDefaultTrueAndIsDeletedFalse(userId)
                    .ifPresent(a -> a.setDefault(false));
        }

        Address saved = addressRepository.save(Address.create(
                userId,
                region.getId(),
                request.nickname().trim(),
                rawRoadAddress,
                rawDetail,
                kakao.latitude(),
                kakao.longitude(),
                isDefault
        ));

        return toResponse(saved);
    }

    public List<AddressResponse> getMyAddresses(UUID userId) {
        return addressRepository.findAllByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AddressResponse getMyAddress(UUID userId, UUID addressId) {
        Address address = addressRepository.findByIdAndUserIdAndIsDeletedFalse(addressId, userId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.ADDRESS_NOT_FOUND));
        return toResponse(address);
    }

    @Transactional
    public AddressResponse updateAddress(UUID userId, UUID addressId, AddressUpdateRequest request) {
        Address address = addressRepository.findByIdAndUserIdAndIsDeletedFalse(addressId, userId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        String rawRoadAddress = request.roadAddress().trim();
        String rawDetail = request.shippingDetailAddress().trim();

        KakaoAddressResult kakao = kakaoAddressClient.searchAddress(rawRoadAddress);

        Region region = regionRepository.findByAddressAndIsDeletedFalse(kakao.depth3Address())
                .orElseThrow(() -> new OminBusinessException(ErrorCode.ADDRESS_REGION_NOT_FOUND));

        // 수정 시 -> 자기 자신 제외하고 상세주소까지 동일이면 중복
        if (addressRepository.existsByUserIdAndRegionIdAndRoadAddressAndShippingDetailAddressAndIsDeletedFalseAndIdNot(
                userId, region.getId(), rawRoadAddress, rawDetail, addressId
        )) {
            throw new OminBusinessException(ErrorCode.ADDRESS_DUPLICATED);
        }

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
                region.getId(),
                request.nickname().trim(),
                rawRoadAddress,
                rawDetail,
                kakao.latitude(),
                kakao.longitude(),
                wantDefault
        );

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