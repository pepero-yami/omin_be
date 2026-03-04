package com.sparta.omin.app.model.review.entity;

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
public class ReviewImage {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @Column(nullable = false)
    private String imageUrl; // S3에서 받은 URL

    @Column(nullable = false)
    private int sequence;


    // 생성자를 통해 관계를 강제
    private ReviewImage(Review review, String imageUrl, int sequence) {
        this.review = review;
        this.imageUrl = imageUrl;
        this.sequence = sequence;
    }

    public static ReviewImage create(Review review, String imageUrl, int sequence) {
        ReviewImage image = new ReviewImage(review, imageUrl, sequence);
        // 생성될 때 부모의 리스트에도 추가해줌 (양방향 편의)
        review.getImages().add(image);
        return image;
    }

}