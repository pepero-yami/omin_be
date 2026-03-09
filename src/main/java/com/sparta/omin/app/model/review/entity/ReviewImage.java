package com.sparta.omin.app.model.review.entity;

import com.sparta.omin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;


@Entity
@Table(name = "p_review_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImage extends BaseEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private int sequence;

    private ReviewImage(Review review, String imageUrl, int sequence) {
        this.review = review;
        this.imageUrl = imageUrl;
        this.sequence = sequence;
    }

    public static ReviewImage create(Review review, String imageUrl, int sequence) {
        ReviewImage image = new ReviewImage(review, imageUrl, sequence);
        review.getImages().add(image);
        return image;
    }
    public void reorder(int newSequence) {
        if (this.sequence != newSequence) {
            this.sequence = newSequence;
        }
    }
}