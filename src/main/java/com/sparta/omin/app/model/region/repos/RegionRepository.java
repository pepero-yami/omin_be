package com.sparta.omin.app.model.region.repos;

import com.sparta.omin.app.model.region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegionRepository extends JpaRepository<Region, UUID> {

    Optional<Region> findByIdAndIsDeletedFalse(UUID id);

    List<Region> findAllByIsDeletedFalseOrderByCreatedAtDesc();

    boolean existsByAddressAndIsDeletedFalse(String address);

    boolean existsByAddressAndIsDeletedFalseAndIdNot(String address, UUID id);
}