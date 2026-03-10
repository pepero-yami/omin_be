package com.sparta.omin.app.model.region.service;

import com.sparta.omin.app.model.region.client.KakaoAddressClient;
import com.sparta.omin.app.model.region.dto.RegionCreateRequest;
import com.sparta.omin.app.model.region.dto.RegionResponse;
import com.sparta.omin.app.model.region.dto.RegionUpdateRequest;
import com.sparta.omin.app.model.region.entity.Region;
import com.sparta.omin.app.model.region.repos.RegionRepository;
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
public class RegionService {

    private final RegionRepository regionRepository;
    private final KakaoAddressClient kakaoAddressClient;

    @Transactional
    public RegionResponse createRegion(RegionCreateRequest request) {
        String rawAddress = request.address().trim();
        String address = kakaoAddressClient.normalizeToRegionDepth3(rawAddress);

        if (regionRepository.existsByAddressAndIsDeletedFalse(address)) {
            throw new OminBusinessException(ErrorCode.REGION_ALREADY_EXISTS);
        }

        Region saved = regionRepository.save(Region.create(address));
        return RegionResponse.of(saved.getId(), saved.getAddress());
    }

    public RegionResponse getRegion(UUID regionId) {
        Region region = regionRepository.findByIdAndIsDeletedFalse(regionId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.REGION_NOT_FOUND));

        return RegionResponse.of(region.getId(), region.getAddress());
    }

    @Transactional
    public RegionResponse updateRegion(UUID regionId, RegionUpdateRequest request) {
        String rawAddress = request.address().trim();
        String address = kakaoAddressClient.normalizeToRegionDepth3(rawAddress);

        Region region = regionRepository.findByIdAndIsDeletedFalse(regionId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.REGION_NOT_FOUND));

        if (regionRepository.existsByAddressAndIsDeletedFalseAndIdNot(address, regionId)) {
            throw new OminBusinessException(ErrorCode.REGION_ALREADY_EXISTS);
        }

        region.updateAddress(address);
        return RegionResponse.of(region.getId(), region.getAddress());
    }

    @Transactional
    public void deleteRegion(UUID regionId) {
        Region region = regionRepository.findByIdAndIsDeletedFalse(regionId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.REGION_NOT_FOUND));

        region.softDelete();
    }

    public Page<RegionResponse> getRegions(String keyword, Pageable pageable) {
        Pageable validatedPageable = validatePageSize(pageable);

        if (keyword != null && !keyword.isBlank()) {
            return regionRepository.findAllByAddressContainingAndIsDeletedFalse(keyword, validatedPageable)
                    .map(r -> RegionResponse.of(r.getId(), r.getAddress()));
        }
        return regionRepository.findAllByIsDeletedFalse(validatedPageable)
                .map(r -> RegionResponse.of(r.getId(), r.getAddress()));
    }

    private Pageable validatePageSize(Pageable pageable) {
        int size = pageable.getPageSize();
        if (size != 10 && size != 30 && size != 50) {
            return PageRequest.of(pageable.getPageNumber(), 10, pageable.getSort());
        }
        return pageable;
    }

    //주소 문자열로 Region ID 조회 - Address 서비스 등 외부 도메인에서 Region 정보가 필요할 때 사용!
    public UUID getRegionIdByAddress(String address) {
        return regionRepository.findByAddressAndIsDeletedFalse(address)
                .map(Region::getId)
                .orElseThrow(() -> new OminBusinessException(ErrorCode.ADDRESS_REGION_NOT_FOUND));
    }
}