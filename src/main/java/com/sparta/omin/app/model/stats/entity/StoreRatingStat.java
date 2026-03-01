package com.sparta.omin.app.model.stats.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


import java.util.UUID;

@Entity
@Table(name = "p_store_rating_stat")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreRatingStat {

    @Id
    @GeneratedValue
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
}