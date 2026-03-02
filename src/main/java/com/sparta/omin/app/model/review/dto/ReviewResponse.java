package com.sparta.omin.app.model.review.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID reviewId,
        UUID orderId,
        UUID userId,
        double rating,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}