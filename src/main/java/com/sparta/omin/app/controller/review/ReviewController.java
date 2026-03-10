package com.sparta.omin.app.controller.review;

import com.sparta.omin.app.model.review.dto.ReviewCreateRequest;
import com.sparta.omin.app.model.review.dto.ReviewCriteria;
import com.sparta.omin.app.model.review.dto.ReviewResponse;
import com.sparta.omin.app.model.review.dto.ReviewUpdateRequest;
import com.sparta.omin.app.model.review.service.ReviewService;
import com.sparta.omin.app.model.user.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@AuthenticationPrincipal User user,
                                                       @Valid @RequestPart ReviewCreateRequest request,
                                                       @Size(max = 5, message = "이미지는 최대 5개까지 업로드할 수 있습니다.")
                                                       @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        ReviewResponse response =
                reviewService.createReview(user, request, images);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.reviewId())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable UUID reviewId) {
        ReviewResponse response = reviewService.getReview(reviewId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false, defaultValue = "DEFAULT") ReviewCriteria criteria,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponse> response = reviewService.getReviews(criteria, pageable, storeId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(@PathVariable UUID reviewId,
                                                       @AuthenticationPrincipal User user,
                                                       @Valid @RequestPart(required = false) ReviewUpdateRequest request,
                                                       @Size(max = 5, message = "이미지는 최대 5개까지 업로드할 수 있습니다.")
                                                       @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        ReviewResponse response = reviewService.updateReview(reviewId, user, request, images);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID reviewId, @AuthenticationPrincipal User user) {
        reviewService.deleteReview(reviewId, user);
        return ResponseEntity.ok().build();
    }
}