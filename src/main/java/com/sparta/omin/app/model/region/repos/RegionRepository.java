package com.sparta.omin.app.model.region.repos;

import com.sparta.omin.app.model.region.entity.Region;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, UUID> {

    Optional<Region> findByIdAndIsDeletedFalse(UUID id);

    List<Region> findAllByIsDeletedFalseOrderByCreatedAtDesc();

    boolean existsByAddressAndIsDeletedFalse(String address);

    boolean existsByAddressAndIsDeletedFalseAndIdNot(String address, UUID id);

    // Seed N+1 제거(한 번에 조회해서 contains로 판별)
    List<Region> findAllByAddressInAndIsDeletedFalse(List<String> address);

    // 주소에 특정 키워드가 포함되어 있고, 삭제되지 않은 지역 목록 조회
    List<Region> findAllByAddressContainingAndIsDeletedFalseOrderByAddressAsc(String keyword);
}