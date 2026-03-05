package com.sparta.omin.app.model.review.dto;

import com.sparta.omin.app.model.review.entity.Review;
import com.sparta.omin.app.model.review.entity.ReviewImage;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record ReviewResponse(
        UUID reviewId,
        UUID orderId,
        UUID userId,
        String nickname,
        double rating,
        String comment,
        List<String> images,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReviewResponse of(Review review, String nickname) {
        return ReviewResponse.builder()
                .reviewId(review.getId())
                .orderId(review.getOrder().getId())
                .userId(review.getUser().getId())
                .nickname(nickname)
                .rating(review.getRating())
                .comment(review.getComment())
                .images(review.getImages().stream()
                        .map(ReviewImage::getImageUrl)
                        .toList())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}