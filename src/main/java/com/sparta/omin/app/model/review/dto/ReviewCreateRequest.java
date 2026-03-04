package com.sparta.omin.app.model.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReviewCreateRequest(
        @NotNull
        UUID orderId,
        @NotNull
        @Min(1)
        @Max(5)
        Double rating,
        String comment
) {}