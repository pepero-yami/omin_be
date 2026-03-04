package com.sparta.omin.app.model.review.entity;

import com.sparta.omin.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "p_review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseAuditEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "review_rating", nullable = false)
    private double rating;

    @Column(name = "review_comment", length = 300)
    private String comment;

    // TODO: 리뷰 이미지 연관
/*    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> images = new ArrayList<>();

    public void addImage(ReviewImage image) {
        images.add(image);
        image.setReview(this);
    }*/

    public static Review create(
            UUID userId,
            UUID orderId,
            double rating,
            String comment
    ) {

        Review review = new Review();
        review.userId = Objects.requireNonNull(userId, "userId must not be null");
        review.orderId = Objects.requireNonNull(orderId, "orderId must not be null");
        validRating(rating);
        review.rating = rating;
        review.comment = comment;
        review.markCreated(userId);

        return review;
    }

    // 도메인 규칙 : 별점
    private static void validRating(double rating) {
        if (rating <= 1 || rating > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }
    }

    public void updateReview(double newRating, String newComment, UUID actorId) {
        this.rating = newRating;
        this.comment = newComment;
        markUpdated(actorId);
    }

    public void softDelete(UUID actorId, LocalDateTime now) {
        markDeleted(actorId, now);
    }

}