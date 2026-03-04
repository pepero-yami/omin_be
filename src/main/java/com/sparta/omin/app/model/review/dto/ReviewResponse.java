package com.sparta.omin.app.model.review.dto;

import com.sparta.omin.app.model.review.entity.Review;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReviewResponse(
        UUID reviewId,
        UUID orderId,
        UUID userId,
        String nickname,
        double rating,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReviewResponse of(Review review, String nickname) {
        return ReviewResponse.builder()
                .reviewId(review.getId())
                .orderId(review.getOrderId())
                .userId(review.getUserId())
                .nickname(nickname)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}