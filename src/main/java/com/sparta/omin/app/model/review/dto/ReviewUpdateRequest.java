package com.sparta.omin.app.model.review.dto;

import com.sparta.omin.app.model.review.entity.ReviewImage;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.ArrayList;
import java.util.List;

public record ReviewUpdateRequest(
        @Min(1)
        @Max(5)
        Double rating,
        String comment,
        List<ReviewImage> updateImages,
        List<String> deleteImages
) {
    public List<String> deleteImageUrls() {
        return new ArrayList<>();
    }

    public List<String> updateImageUrls() {
        return  new ArrayList<>();
    }
}