package com.sparta.omin.app.model.review.entity;

import com.sparta.omin.common.entity.BaseAuditEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseAuditEntity {

    @Id
    @GeneratedValue
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
            String comment,
            UUID actorId
    ) {
        // 도메인 규칙 : 별점
    /*    if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("rating must be between 0 and 5");
        }*/

        Review review = new Review();
        review.userId = userId;
        review.orderId = orderId;
        review.rating = rating;
        review.comment = comment;
        review.markCreated(actorId);

        return review;
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