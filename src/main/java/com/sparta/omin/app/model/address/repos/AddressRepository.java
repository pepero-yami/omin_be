package com.sparta.omin.app.model.address.repos;

import com.sparta.omin.app.model.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findAllByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId);

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