package com.sparta.omin.app.model.review.dto;

import com.sparta.omin.app.model.review.entity.ReviewImage;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReviewUpdateRequest(
        @DecimalMin(value = "1.0")
        @DecimalMax(value = "5.0")
        Double rating,

        @Size(max = 300, message = "코멘트는 300자를 넘길 수 없습니다.")
        String comment,
        List<ReviewImage> updateImages,
        List<String> deleteImages
) {
    public List<String> deleteImageUrls() {
        return deleteImages;
    }

    public List<ReviewImage> updateImages() {
        return updateImages;
    }
}