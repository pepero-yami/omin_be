package com.sparta.omin.app.model.review.entity;

import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.store.entity.Store;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "p_review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "order_id", nullable = false, updatable = false, unique = true)
    @OneToOne(fetch = FetchType.LAZY)
    private Order order;

    @JoinColumn(name = "store_id", nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Store store;

    @Column(name = "review_rating", nullable = false)
    private double rating;

    @Column(name = "review_comment", length = 300)
    private String comment;

    @SQLRestriction("is_deleted = false")
    @BatchSize(size = 100)
    @OrderBy("sequence ASC")
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
    private List<ReviewImage> images = new ArrayList<>();

    public static Review create(
            User user,
            Order order,
            Store store,
            double rating,
            String comment
    ) {

        Review review = new Review();
        review.user = Objects.requireNonNull(user, "user must not be null");
        review.order = Objects.requireNonNull(order, "order must not be null");
        review.store = Objects.requireNonNull(store, "store must not be null");
        validRating(rating);
        review.rating = rating;
        review.comment = comment;

        return review;
    }

    // 도메인 규칙 : 별점
    private static void validRating(double rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }
    }

    public void updateReview(double newRating, String newComment) {
        this.rating = newRating;
        this.comment = newComment;
    }

    public void addImages(List<String> imageUrls) {
        this.images.clear();
        for (int i = 0; i < imageUrls.size(); i++) {
            ReviewImage.create(this, imageUrls.get(i), i);
        }
    }

    /**
     * BaseEntity를 상속받지 않는 ReviewImage 객체가 수정될 때
     * updatedAt 갱신 (서버-DB 시간 같은지??)
     */
    public void markUpdated() {
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        this.isDeleted = true;
    }
}