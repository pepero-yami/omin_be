package com.sparta.omin.app.model.region.service;

import com.sparta.omin.app.model.region.dto.RegionCreateRequest;
import com.sparta.omin.app.model.region.dto.RegionResponse;
import com.sparta.omin.app.model.region.dto.RegionUpdateRequest;
import com.sparta.omin.app.model.region.entity.Region;
import com.sparta.omin.app.model.region.repos.RegionRepository;
import com.sparta.omin.common.util.AuditUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RegionService {

    private final RegionRepository regionRepository;

    public RegionService(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    @Transactional
    public RegionResponse create(RegionCreateRequest request) {
        String address = request.getAddress().trim();

        if (regionRepository.existsByAddressAndIsDeletedFalse(address)) {
            // TODO(error): 409 Conflict로 내려주는 커스텀 예외로 교체하는 게 좋을듯
            throw new IllegalStateException("이미 존재하는 지역(address)입니다.");
        }

        UUID actorId = AuditUserProvider.currentUserId(); // TODO(auth): 인증 붙이면 실제 로그인 유저로 변경
        // createdAt/updatedAt은 JPA Auditing(@CreatedDate/@LastModifiedDate)으로 자동 세팅
        Region region = Region.create(UUID.randomUUID(), address, actorId);
        Region saved = regionRepository.save(region);

        return RegionResponse.of(saved.getId(), saved.getAddress());
    }

    public RegionResponse get(UUID regionId) {
        Region region = regionRepository.findByIdAndIsDeletedFalse(regionId)
                // TODO(error): 404 Not Found로 내려주는 커스텀 예외로 교체하는 게 좋을듯
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역(regionId)입니다."));

        return RegionResponse.of(region.getId(), region.getAddress());
    }

    @Transactional
    public RegionResponse update(UUID regionId, RegionUpdateRequest request) {
        String address = request.getAddress().trim();

        Region region = regionRepository.findByIdAndIsDeletedFalse(regionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역(regionId)입니다."));

        if (regionRepository.existsByAddressAndIsDeletedFalseAndIdNot(address, regionId)) {
            // TODO(error): 409 Conflict로 내려주는 커스텀 예외로 교체하는 게 좋을지도...?
            throw new IllegalStateException("이미 존재하는 지역(address)입니다.");
        }

        UUID actorId = AuditUserProvider.currentUserId(); // TODO(auth): 인증 붙이면 실제 로그인 유저로 변경
        // updatedAt은 JPA Auditing(@LastModifiedDate)으로 자동 갱신
        region.updateAddress(address, actorId);

        return RegionResponse.of(region.getId(), region.getAddress());
    }

    @Transactional
    public void delete(UUID regionId) {
        Region region = regionRepository.findByIdAndIsDeletedFalse(regionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역(regionId)입니다."));

        UUID actorId = AuditUserProvider.currentUserId(); // TODO(auth): 인증 붙이면 실제 로그인 유저로 변경
        // deletedAt은 Auditing 대상이 아니므로 삭제 시점 now를 여기서만 세팅
        LocalDateTime now = LocalDateTime.now();
        region.softDelete(actorId, now);
    }

    public List<RegionResponse> list() {
        return regionRepository.findAllByIsDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(r -> RegionResponse.of(r.getId(), r.getAddress()))
                .toList();
    }
}