package com.sparta.omin.app.model.region.service;

import com.sparta.omin.app.model.region.client.KakaoAddressClient;
import com.sparta.omin.app.model.region.dto.RegionCreateRequest;
import com.sparta.omin.app.model.region.dto.RegionResponse;
import com.sparta.omin.app.model.region.dto.RegionUpdateRequest;
import com.sparta.omin.app.model.region.entity.Region;
import com.sparta.omin.app.model.region.repos.RegionRepository;
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
    public RegionResponse createRegion(RegionCreateRequest request, UUID actorId) {
        String rawAddress = request.address().trim();
        String address = kakaoAddressClient.normalizeToRegionDepth3(rawAddress);

        if (regionRepository.existsByAddressAndIsDeletedFalse(address)) {
            throw new IllegalStateException("이미 존재하는 지역(address)입니다.");
        }

        Region region = Region.create(address, actorId);
        Region saved = regionRepository.save(region);

        return RegionResponse.of(saved.getId(), saved.getAddress());
    }

    public RegionResponse getRegion(UUID regionId) {
        Region region = regionRepository.findByIdAndIsDeletedFalse(regionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역(regionId)입니다."));

        return RegionResponse.of(region.getId(), region.getAddress());
    }

    @Transactional
    public RegionResponse updateRegion(UUID regionId, RegionUpdateRequest request, UUID actorId) {
        String rawAddress = request.address().trim();
        String address = kakaoAddressClient.normalizeToRegionDepth3(rawAddress);

        Region region = regionRepository.findByIdAndIsDeletedFalse(regionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역(regionId)입니다."));

        if (regionRepository.existsByAddressAndIsDeletedFalseAndIdNot(address, regionId)) {
            throw new IllegalStateException("이미 존재하는 지역(address)입니다.");
        }

        region.updateAddress(address, actorId);

        return RegionResponse.of(region.getId(), region.getAddress());
    }

    @Transactional
    public void deleteRegion(UUID regionId, UUID actorId) {
        Region region = regionRepository.findByIdAndIsDeletedFalse(regionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역(regionId)입니다."));

        // deleted_at/deleted_by는 제거됨. 삭제자는 updatedBy로 대체.
        region.softDelete(actorId);
    }

    public List<RegionResponse> getRegions() {
        return regionRepository.findAllByIsDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(r -> RegionResponse.of(r.getId(), r.getAddress()))
                .toList();
    }
}