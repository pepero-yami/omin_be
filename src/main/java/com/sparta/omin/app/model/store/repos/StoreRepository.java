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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    @Query("SELECT s FROM Store s LEFT JOIN FETCH s.images WHERE s.id = :id")
    Optional<Store> findByIdWithImages(@Param("id") UUID id);

    @Query(value = """
            SELECT
                s.id as storeId,
                s.category,
                s.name,
                s.road_address as roadAddress,
                s.detail_address as detailAddress,
                s.status,
                si.image_url as mainImage,
                ST_Distance(s.coordinates, :center) as distance
            FROM p_store s
            JOIN p_store_image si ON s.id = si.store_id AND si.sequence = 1 AND si.is_deleted = false
            WHERE s.is_deleted = false
              AND ST_DWithin(s.coordinates, :center, :radius)
              AND s.category = :#{#category.name()}
              AND s.status != 'PENDING'
              AND (:name IS NULL OR s.name ILIKE CONCAT('%', :name, '%'))
              AND (:lastId IS NULL
                   OR ST_Distance(s.coordinates, :center) > :lastDistance
                   OR (ST_Distance(s.coordinates, :center) = :lastDistance AND s.id > :lastId))
            ORDER BY ST_Distance(s.coordinates, :center), s.id
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

    List<Store> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    List<Store> findByStatusOrderByCreatedAtDesc(Status status);

    boolean existsByRoadAddressAndDetailAddress(String roadAddress, String detailAddress);

    boolean existsByRoadAddressAndDetailAddressAndIdNot(String roadAddress, String detailAddress, UUID id);

    interface StoreSearchProjection {
        UUID getStoreId();
        Category getCategory();
        String getName();
        String getRoadAddress();
        String getDetailAddress();
        Status getStatus();
        String getMainImage();
        Double getDistance();
    }
}
