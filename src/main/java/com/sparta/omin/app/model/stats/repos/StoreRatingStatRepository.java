package com.sparta.omin.app.model.stats.repos;

import com.sparta.omin.app.model.stats.entity.StoreRatingStat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface StoreRatingStatRepository extends JpaRepository<StoreRatingStat, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StoreRatingStat s where s.storeId = :storeId")
    Optional<StoreRatingStat> findByStoreIdWithLock(@Param("storeId") UUID id);
}
