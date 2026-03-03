package com.sparta.omin.app.model.review.service;

import com.sparta.omin.app.model.order.entity.Order;
import com.sparta.omin.app.model.order.repos.OrderRepository;
import com.sparta.omin.app.model.review.dto.ReviewCreateRequest;
import com.sparta.omin.app.model.review.dto.ReviewResponse;
import com.sparta.omin.app.model.review.entity.Review;
import com.sparta.omin.app.model.review.repos.ReviewRepository;
import com.sparta.omin.app.model.stats.entity.StoreRatingStat;
import com.sparta.omin.app.model.stats.repos.StoreRatingStatRepository;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final StoreRatingStatRepository statRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public ReviewResponse createReview(UUID userId, ReviewCreateRequest request) {

        // 1. 주문 조회
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_NOT_FOUND));

        // 2. 주문 상태 COMPLETED 아니면 예외
        if (!order.isCompleted()) {
            throw new ApiException(ErrorCode.ORDER_NOT_COMPLETED);
        }
        // 3. 주문일 + 2일 초과면 예외
        if (order.getCreatedAt().
                plusDays(2).
                isBefore(LocalDateTime.now())
        ) {
            throw new ApiException(ErrorCode.REVIEW_PERIOD_EXPIRED);
        }

        // 4. 이미 리뷰 작성했으면 예외
        Optional<Review> oldReview = reviewRepository.findByOrderId(request.orderId());
        if (oldReview.isPresent()) {
            throw new ApiException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        // 5. Review 생성
        Review newReview = Review.create(
                userId, // TODO: securityContext
                request.orderId(),
                request.rating(),
                request.comment()
                // TODO: image
        );
        reviewRepository.save(newReview);

        // 7. 해당 가게에 기존 평점 통계가 존재하는지 확인
        Optional<StoreRatingStat> oldStoreRatingStat = statRepository.findByStoreId((order.getStoreId()));

        if (oldStoreRatingStat.isPresent()) {
            oldStoreRatingStat.get().increase(request.rating());
        } else {
            statRepository.save(StoreRatingStat.create(order.getStoreId(), request.rating()));
        }
        // 8. 단일 응답 생성
        return ReviewResponse.from(newReview);
    }
}
