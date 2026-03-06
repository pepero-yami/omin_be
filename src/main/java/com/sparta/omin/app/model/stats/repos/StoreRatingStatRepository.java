package com.sparta.omin.app.model.stats.repos;

import com.sparta.omin.app.model.stats.entity.StoreRatingStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StoreRatingStatRepository extends JpaRepository<StoreRatingStat, UUID> {
    Optional<StoreRatingStat> findByStoreId(UUID storeId);
}
