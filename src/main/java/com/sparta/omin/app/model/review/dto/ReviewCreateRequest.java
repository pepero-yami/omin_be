package com.sparta.omin.app.model.review.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

public record ReviewCreateRequest(
        @NotNull
        UUID orderId,
        @NotNull
        @DecimalMin(value = "1.0")
        @DecimalMax(value = "5.0")
        Double rating,
        @Length(max = 300, message = "코멘트는 300자를 넘길 수 없습니다.")
        String comment
) {
}