package com.sparta.omin.app.model.region.service;

import com.sparta.omin.app.model.region.client.KakaoAddressClient;
import com.sparta.omin.app.model.region.dto.RegionCreateRequest;
import com.sparta.omin.app.model.region.dto.RegionResponse;
import com.sparta.omin.app.model.region.dto.RegionUpdateRequest;
import com.sparta.omin.app.model.region.entity.Region;
import com.sparta.omin.app.model.region.repos.RegionRepository;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
            throw new ApiException(ErrorCode.REGION_ALREADY_EXISTS);
        }

        Region saved = regionRepository.save(Region.create(address));
        return RegionResponse.of(saved.getId(), saved.getAddress());
    }

    public RegionResponse getRegion(UUID regionId) {
        Region region = regionRepository.findByIdAndIsDeletedFalse(regionId)
                .orElseThrow(() -> new ApiException(ErrorCode.REGION_NOT_FOUND));

        return RegionResponse.of(region.getId(), region.getAddress());
    }

    @Transactional
    public RegionResponse updateRegion(UUID regionId, RegionUpdateRequest request) {
        String rawAddress = request.address().trim();
        String address = kakaoAddressClient.normalizeToRegionDepth3(rawAddress);

        Region region = regionRepository.findByIdAndIsDeletedFalse(regionId)
                .orElseThrow(() -> new ApiException(ErrorCode.REGION_NOT_FOUND));

        if (regionRepository.existsByAddressAndIsDeletedFalseAndIdNot(address, regionId)) {
            throw new ApiException(ErrorCode.REGION_ALREADY_EXISTS);
        }

        region.updateAddress(address);
        return RegionResponse.of(region.getId(), region.getAddress());
    }

    @Transactional
    public void deleteRegion(UUID regionId) {
        Region region = regionRepository.findByIdAndIsDeletedFalse(regionId)
                .orElseThrow(() -> new ApiException(ErrorCode.REGION_NOT_FOUND));

        region.softDelete();
    }

    public List<RegionResponse> getRegions() {
        return regionRepository.findAllByIsDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(r -> RegionResponse.of(r.getId(), r.getAddress()))
                .toList();
    }
}