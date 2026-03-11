package com.sparta.omin.app.model.store.repos;

import com.sparta.omin.app.model.store.code.Category;
import com.sparta.omin.app.model.store.code.Status;
import com.sparta.omin.app.model.store.entity.Store;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    @Query("SELECT s FROM Store s JOIN FETCH s.images i WHERE s.id = :id ORDER BY i.sequence")
    Optional<Store> findByIdWithImages(@Param("id") UUID id);

    @Query(value = """
            SELECT t.storeId, t.category, t.name, t.roadAddress, t.detailAddress,
                   t.status, t.mainImage, t.distance, t.avgRating, t.totalReview
            FROM (
                SELECT s.id          as storeId,
                       s.category,
                       s.name,
                       s.road_address  as roadAddress,
                       s.detail_address as detailAddress,
                       s.status,
                       si.url   as mainImage,
                       ST_Distance(s.coordinates, cast(:center as geography)) as distance,
                       srs.avg_rating  as avgRating,
                       srs.total_review as totalReview
                FROM p_store s
                JOIN p_store_image si ON s.id = si.store_id AND si.sequence = 1 AND si.is_deleted = false
                LEFT JOIN p_store_rating_stat srs ON s.id = srs.store_id
                WHERE s.is_deleted = false
                  AND ST_DWithin(s.coordinates, cast(:center as geography), :radius)
                  AND s.category = :#{#category.name()}
                  AND s.status != 'PENDING'
                  AND (:name IS NULL OR s.name ILIKE CONCAT('%', :name, '%'))
            ) t
            WHERE (:lastId IS NULL OR :lastDistance IS NULL
                   OR t.distance > :lastDistance
                   OR (t.distance = :lastDistance AND t.storeId > :lastId))
            ORDER BY t.distance, t.storeId
            """, nativeQuery = true)
    Slice<StoreSearchProjection> findByCenterAndRadiusOrderByDistance(
            @Param("center") Point center,
            @Param("radius") double radius,
            @Param("category") Category category,
            @Param("name") String name,
            @Param("lastDistance") Double lastDistance,
            @Param("lastId") UUID lastId,
            Pageable pageable
    );

    interface StoreSearchProjection {
        UUID getStoreId();
        Category getCategory();
        String getName();
        String getRoadAddress();
        String getDetailAddress();
        Status getStatus();
        Double getDistance();
        Double getAvgRating();
        Long getTotalReview();
        String getMainImage();
    }

    @Query(value = """
            SELECT s.* FROM p_store s
            WHERE s.is_deleted = false
              AND s.owner_id = :ownerId
              AND (CAST(:lastCreatedAt AS timestamp) IS NULL
                   OR s.created_at < CAST(:lastCreatedAt AS timestamp)
                   OR (s.created_at = CAST(:lastCreatedAt AS timestamp)
                       AND s.id < CAST(:lastId AS uuid)))
            ORDER BY s.created_at DESC, s.id DESC
            """, nativeQuery = true)
    Slice<Store> findByOwnerIdCursor(
            @Param("ownerId") UUID ownerId,
            @Param("lastCreatedAt") LocalDateTime lastCreatedAt,
            @Param("lastId") UUID lastId,
            Pageable pageable
    );

    @Query(value = """
            SELECT s.* FROM p_store s
            WHERE s.is_deleted = false
              AND s.status = :status
              AND (CAST(:lastCreatedAt AS timestamp) IS NULL
                   OR s.created_at < CAST(:lastCreatedAt AS timestamp)
                   OR (s.created_at = CAST(:lastCreatedAt AS timestamp)
                       AND s.id < CAST(:lastId AS uuid)))
            ORDER BY s.created_at DESC, s.id DESC
            """, nativeQuery = true)
    Slice<Store> findByStatusCursor(
            @Param("status") String status,
            @Param("lastCreatedAt") LocalDateTime lastCreatedAt,
            @Param("lastId") UUID lastId,
            Pageable pageable
    );

    boolean existsByRoadAddressAndDetailAddress(String roadAddress, String detailAddress);

    boolean existsByRoadAddressAndDetailAddressAndIdNot(String roadAddress, String detailAddress, UUID id);

    Optional<Store> findByIdAndIsDeletedFalse(UUID storeId);

    long countByOwnerId(UUID ownerId);
}
