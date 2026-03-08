package com.sparta.omin.app.model.review.repos;

import com.sparta.omin.app.model.review.entity.Review;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    boolean existsByOrder_IdAndIsDeletedFalse(@NotNull UUID id);

    @EntityGraph(attributePaths = {"user", "order", "images"})
    Optional<Review> findByIdAndIsDeletedFalse(@Param("reviewId") UUID reviewId);

    @EntityGraph(attributePaths = {"user", "order"})
    Page<Review> findAllByIsDeletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "order", "store"})
    Page<Review> findAllByStoreIdAndIsDeletedFalse(UUID storeId, Pageable pageable);
}
