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

    @Query("SELECT s FROM Store s LEFT JOIN FETCH s.images WHERE s.id = :id")
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
                       si.image_url   as mainImage,
                       ST_Distance(s.coordinates, :center::geography) as distance,
                       srs.avg_rating  as avgRating,
                       srs.total_review as totalReview
                FROM p_store s
                JOIN p_store_image si ON s.id = si.store_id AND si.sequence = 1 AND si.is_deleted = false
                LEFT JOIN p_store_rating_stat srs ON s.id = srs.store_id
                WHERE s.is_deleted = false
                  AND ST_DWithin(s.coordinates, :center::geography, :radius)
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

    @Query("SELECT s FROM Store s WHERE s.ownerId = :ownerId " +
            "AND (:lastCreatedAt IS NULL OR s.createdAt < :lastCreatedAt " +
            "     OR (s.createdAt = :lastCreatedAt AND s.id < :lastId)) " +
            "ORDER BY s.createdAt DESC, s.id DESC")
    Slice<Store> findByOwnerIdCursor(
            @Param("ownerId") UUID ownerId,
            @Param("lastCreatedAt") LocalDateTime lastCreatedAt,
            @Param("lastId") UUID lastId,
            Pageable pageable
    );

    @Query("SELECT s FROM Store s WHERE s.status = :status " +
            "AND (:lastCreatedAt IS NULL OR s.createdAt < :lastCreatedAt " +
            "     OR (s.createdAt = :lastCreatedAt AND s.id < :lastId)) " +
            "ORDER BY s.createdAt DESC, s.id DESC")
    Slice<Store> findByStatusCursor(
            @Param("status") Status status,
            @Param("lastCreatedAt") LocalDateTime lastCreatedAt,
            @Param("lastId") UUID lastId,
            Pageable pageable
    );

    boolean existsByRoadAddressAndDetailAddress(String roadAddress, String detailAddress);

    boolean existsByRoadAddressAndDetailAddressAndIdNot(String roadAddress, String detailAddress, UUID id);

    Optional<Store> findByIdAndIsDeletedFalse(UUID storeId);

    long countByOwnerId(UUID ownerId);
}
