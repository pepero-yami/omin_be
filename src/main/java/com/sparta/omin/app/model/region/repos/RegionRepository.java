package com.sparta.omin.app.model.region.repos;

import com.sparta.omin.app.model.region.entity.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegionRepository extends JpaRepository<Region, UUID> {

    Optional<Region> findByIdAndIsDeletedFalse(UUID id);

    Page<Region> findAllByIsDeletedFalse(Pageable pageable);

    boolean existsByAddressAndIsDeletedFalse(String address);

    boolean existsByAddressAndIsDeletedFalseAndIdNot(String address, UUID id);

    // Seed N+1 제거(한 번에 조회해서 contains로 판별)
    List<Region> findAllByAddressInAndIsDeletedFalse(List<String> address);

    // 주소에 특정 키워드가 포함되어 있고, 삭제되지 않은 지역 목록 조회
    Page<Region> findAllByAddressContainingAndIsDeletedFalse(String keyword, Pageable pageable);;

    // Address에서 단일 주소 검증용
    Optional<Region> findByAddressAndIsDeletedFalse(String address);
}