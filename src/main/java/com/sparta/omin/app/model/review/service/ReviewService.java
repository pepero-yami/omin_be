package com.sparta.omin.app.model.review.service;

import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.repos.OrderRepository;
import com.sparta.omin.app.model.review.dto.ReviewCreateRequest;
import com.sparta.omin.app.model.review.dto.ReviewResponse;
import com.sparta.omin.app.model.review.entity.Review;
import com.sparta.omin.app.model.review.repos.ReviewRepository;
import com.sparta.omin.app.model.stats.entity.StoreRatingStat;
import com.sparta.omin.app.model.stats.repos.StoreRatingStatRepository;
import com.sparta.omin.app.model.user.constants.Role;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.constants.ErrorCode;
import com.sparta.omin.common.util.ImageUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final StoreRatingStatRepository statRepository;
    private final OrderRepository orderRepository;
    private final ImageUploader imageUploader;

    @Transactional
    public ReviewResponse createReview(User user, ReviewCreateRequest request, List<MultipartFile> images) {
        UUID loginUserId = user.getId();
        // 이미지 개수 초과 예외
        if (images != null && images.size() > 5) throw new ApiException(ErrorCode.REVIEW_IMAGE_COUNT_EXCEEDED);
        // 사용자가 요청한 주문 조회
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_NOT_FOUND));
        // 로그인된 사용자 자신의 주문이 아니라면 예외
        if (!loginUserId.equals(order.getUser().getId())) throw new ApiException(ErrorCode.ORDER_USER_MISMATCH);
        // 사용자 자신의 가게라면 예외
        if (user.getRole() == Role.OWNER && order.getStore().getOwnerId().equals(loginUserId)) {
            throw new ApiException(ErrorCode.SELF_REVIEW_NOT_ALLOWED);
        }
        // 주문 상태 COMPLETED 아니면 예외
        if (!order.isCompleted()) throw new ApiException(ErrorCode.ORDER_NOT_COMPLETED);

        // 주문일 + 2일 초과면 예외 --> status 변경시간 + 2일
        if (order.getCreatedAt().
                plusDays(2).
                isBefore(LocalDateTime.now())
        ) {
            throw new ApiException(ErrorCode.REVIEW_PERIOD_EXPIRED);
        }
        // 이미 리뷰 작성했으면 예외
        if (reviewRepository.existsByOrder_IdAndIsDeletedFalse(order.getId()))
            throw new ApiException(ErrorCode.REVIEW_ALREADY_EXISTS);

        // Review 생성
        Review newReview = Review.create(
                user,
                order,
                order.getStore(),
                request.rating(),
                request.comment()
        );
        // 이미지 처리 (이미지가 있을 경우에만)
        if (images != null && !images.isEmpty()) {
            List<String> uploadedUrls = images.stream()
                    .filter(file -> !file.isEmpty())
                    .map(imageUploader::uploadReviewImage)
                    .toList();

            newReview.addImages(uploadedUrls); // 순서대로 들어간 리스트 전달
        }
        reviewRepository.save(newReview);

        // 주문에 해당하는 가게에 기존 평점 통계가 존재하는지 확인 후 평점 생성 / 업데이트
        Optional<StoreRatingStat> oldStoreRatingStat = statRepository.findByStoreId(order.getStore().getId());
        if (oldStoreRatingStat.isPresent()) {
            oldStoreRatingStat.get().increase(request.rating());
        } else {
            statRepository.save(StoreRatingStat.create(order.getStore().getId(), request.rating()));
        }
        return ReviewResponse.from(newReview);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReview(UUID reviewId) {
        Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId).orElseThrow(() -> new ApiException(ErrorCode.REVIEW_NOT_FOUND));
        return ReviewResponse.from(review);
    }
}
