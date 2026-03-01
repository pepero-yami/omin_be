package com.sparta.omin.app.model.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.UUID;

public record ReviewCreateRequest(
        UUID userId,
        UUID orderId,
        @Min(0)
        @Max(5)
        double rating,
        String comment,
        UUID actorId
) {
}
