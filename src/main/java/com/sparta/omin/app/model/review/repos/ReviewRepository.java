package com.sparta.omin.app.model.review.repos;

import com.sparta.omin.app.model.review.entity.Review;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Optional<Review> findByOrderIdAndIsDeletedFalse(@NotNull UUID uuid);
}
