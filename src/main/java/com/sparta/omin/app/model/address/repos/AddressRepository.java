package com.sparta.omin.app.model.address.repos;

import com.sparta.omin.app.model.address.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    Page<Address> findAllByUserIdAndIsDeletedFalse(UUID userId, Pageable pageable);

    Optional<Address> findByIdAndUserIdAndIsDeletedFalse(UUID id, UUID userId);

    boolean existsByUserIdAndIsDeletedFalse(UUID userId);

    long countByUserIdAndIsDeletedFalse(UUID userId);

    Optional<Address> findByUserIdAndIsDefaultTrueAndIsDeletedFalse(UUID userId);

    boolean existsByUserIdAndRegionIdAndRoadAddressAndShippingDetailAddressAndIsDeletedFalse(
            UUID userId,
            UUID regionId,
            String roadAddress,
            String shippingDetailAddress
    );

    boolean existsByUserIdAndRegionIdAndRoadAddressAndShippingDetailAddressAndIsDeletedFalseAndIdNot(
            UUID userId,
            UUID regionId,
            String roadAddress,
            String shippingDetailAddress,
            UUID idNot
    );
}