package com.sparta.omin.app.model.review.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
    UUID reviewId,
    UUID storeId,
    double reviewRating,
    String reviewComment,
    LocalDateTime createdAt
) { }
