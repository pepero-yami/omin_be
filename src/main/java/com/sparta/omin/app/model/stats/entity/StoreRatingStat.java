package com.sparta.omin.app.model.stats.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Entity
@Table(name = "p_store_rating_stat")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreRatingStat {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "store_id", nullable = false, unique = true)
    private UUID storeId;

    @Column(name = "total_review", nullable = false)
    private long totalReview;

    @Column(name = "total_rating", nullable = false)
    private double totalRating;

    @Column(name = "avg_rating", nullable = false)
    private double avgRating;

    public static StoreRatingStat create(UUID storeId, double initialRating) {
        StoreRatingStat storeRatingStat = new StoreRatingStat();
        storeRatingStat.storeId = storeId;
        storeRatingStat.totalReview = 1;
        storeRatingStat.totalRating = initialRating;
        storeRatingStat.avgRating = initialRating;
        return storeRatingStat;
    }

    public void increase(double rating) {
        this.totalReview++;
        this.totalRating += rating;
        this.avgRating = totalRating / totalReview;
    }

    public void decrease(double rating) {
        this.totalReview--;
        this.totalRating -= rating;
        this.avgRating = totalReview == 0 ? 0 : totalRating / totalReview;
    }

    public void updateRatingByDiff(double ratingDiff) {
        this.totalRating += ratingDiff;
        this.avgRating = totalRating / totalReview;
    }
}